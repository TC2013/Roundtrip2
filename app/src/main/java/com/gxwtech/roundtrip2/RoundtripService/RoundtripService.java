package com.gxwtech.roundtrip2.RoundtripService;

import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.gxwtech.roundtrip2.RT2Const;
import com.gxwtech.roundtrip2.RoundtripService.RileyLink.PumpManager;
import com.gxwtech.roundtrip2.RoundtripService.RileyLinkBLE.RFSpy;
import com.gxwtech.roundtrip2.RoundtripService.RileyLinkBLE.RileyLinkBLE;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.Page;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;
import com.gxwtech.roundtrip2.util.ByteUtil;

import java.util.ArrayList;

/**
 * RoundtripService is intended to stay running when the gui-app is closed.
 */
public class RoundtripService extends Service {
    private static final String TAG="RoundtripService";
    private static final String WAKELOCKNAME = "com.gxwtech.roundtrip2.RoundtripServiceWakeLock";
    private static volatile PowerManager.WakeLock lockStatic = null;

    private boolean needBluetoothPermission = true;

    private Messenger mMessenger;
    private boolean mBound = false;
    private Handler mMessageHandler;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private ArrayList<Messenger> mClients = new ArrayList<>();

    // saved settings

    private SharedPreferences sharedPref;
    private String pumpIDString;
    private byte[] pumpIDBytes;
    private String mRileylinkAddress;

    // Our hardware/software connection
    private RileyLinkBLE rileyLinkBLE; // android-bluetooth management
    private RFSpy rfspy; // interface for 916MHz radio.
    private PumpManager pumpManager; // interface to Minimed

    public RoundtripService() {
        super();
        Log.d(TAG, "RoundtripService newly constructed");
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mContext = getApplicationContext();
        mMessageHandler = new MessageHandler();
        mMessenger = new Messenger(mMessageHandler);

        sharedPref = mContext.getSharedPreferences(RT2Const.serviceLocal.sharedPreferencesKey, Context.MODE_PRIVATE);

        // get most recently used pumpID
        pumpIDString = sharedPref.getString(RT2Const.serviceLocal.pumpIDKey,"000000");
        pumpIDBytes = ByteUtil.fromHexString(pumpIDString);
        if (pumpIDBytes.length != 3) {
            Log.e(TAG,"Invalid pump ID? " + ByteUtil.shortHexString(pumpIDBytes));
            pumpIDBytes = new byte[] {0,0,0};
            pumpIDString = "000000";
        }
        if (pumpIDString.equals("000000")) {
            Log.e(TAG,"Using pump ID "+pumpIDString);
        } else {
            Log.i(TAG,"Using pump ID "+pumpIDString);
        }

        // get most recently used RileyLink address
        mRileylinkAddress = sharedPref.getString(RT2Const.serviceLocal.rileylinkAddressKey,"");

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            /* here we can listen for local broadcasts, then send ourselves
             * a specific intent to deal with them, if we wish
              */
                if (intent == null) {
                    Log.e(TAG,"onReceive: received null intent");
                } else {
                    String action = intent.getAction();
                    if (action == null) {
                        Log.e(TAG,"onReceive: null action");
                    } else {
                        if (action.equals(RT2Const.serviceLocal.bluetooth_connected)) {
                            rileyLinkBLE.discoverServices();
                            // If this is successful,
                            // We will get a broadcast of RT2Const.serviceLocal.BLE_services_discovered
                        } else if (action.equals(RT2Const.serviceLocal.BLE_services_discovered)) {
                            rfspy = new RFSpy(context, rileyLinkBLE);
                            rfspy.startReader(); // call startReader from outside?
                            Log.i(TAG, "Announcing RileyLink open For business");
                            sendMessage(RT2Const.IPC.MSG_BLE_RileyLinkReady);
                            pumpManager = new PumpManager(rfspy, pumpIDBytes);
                            PumpModel reportedPumpModel = pumpManager.getPumpModel();
                            if (!reportedPumpModel.equals(PumpModel.UNSET)) {
                                sendMessage(RT2Const.IPC.MSG_PUMP_pumpFound);
                            } else {
                                sendMessage(RT2Const.IPC.MSG_PUMP_pumpLost);
                            }
                        } else if (action.equals(RT2Const.serviceLocal.ipcBound)) {
                            // If we still need permission for bluetooth, ask now.
                            if (needBluetoothPermission) {
                                sendBLERequestForAccess();
                            }
                        } else {
                            Log.e(TAG,"Unhandled broadcast: action="+action);
                        }
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RT2Const.serviceLocal.bluetooth_connected);
        intentFilter.addAction(RT2Const.serviceLocal.bluetooth_disconnected);
        intentFilter.addAction(RT2Const.serviceLocal.BLE_services_discovered);
        intentFilter.addAction(RT2Const.serviceLocal.ipcBound);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, intentFilter);

        Log.d(TAG, "onCreate(): It's ALIVE!");

        rileyLinkBLE = new RileyLinkBLE(this);
        if (mRileylinkAddress.length() > 0) {
            rileyLinkBLE.findRileyLink(mRileylinkAddress);
        }
    }

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: Received message " + msg);
            Bundle bundle = msg.getData();
            switch(msg.what) {
                // This just helps sub-divide the message processing
                case RT2Const.IPC.MSG_registerClient:
                    // send a reply, to let them know we're listening.
                    Message myReply = Message.obtain(null, RT2Const.IPC.MSG_clientRegistered,0,0);
                    try {
                        msg.replyTo.send(myReply);
                        mClients.add(msg.replyTo);
                        Log.d(TAG,"handleMessage: Registered client");
                    } catch (RemoteException e) {
                        // I guess they aren't registered after all...
                        Log.e(TAG,"handleMessage: failed to send acknowledgement of registration");
                    }

                    break;
                case RT2Const.IPC.MSG_IPC:
                    if (!handleIPCMessage(msg)) {
                        super.handleMessage(msg);
                    }
                    break;
                /*
                case RT2Const.MSG_ping:
                    String hello = (String)bundle.get("key_hello");
                    Toast.makeText(mContext,hello, Toast.LENGTH_SHORT).show();
                    break;
                    */
                default:
                    Log.e(TAG,"handleMessage: unknown 'what' in message: "+msg.what);
                    super.handleMessage(msg);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        mBound = true;
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(RT2Const.serviceLocal.ipcBound));
        return mMessenger.getBinder();
    }

    // Here is where the wake-lock begins:
    // We've received a service startCommand, we grab the lock.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (intent != null) {
            PowerManager.WakeLock lock = getLock(this.getApplicationContext());

            if (!lock.isHeld() || (flags & START_FLAG_REDELIVERY) != 0) {
                lock.acquire();
            }

            // This will end up running onHandleIntent
            super.onStartCommand(intent, flags, startId);
        } else {
            Log.e(TAG, "Received null intent?");
        }
        BluetoothInit();
        return (START_REDELIVER_INTENT | START_STICKY);
    }
    synchronized private static PowerManager.WakeLock getLock(Context context) {
        if (lockStatic == null) {
            PowerManager mgr =
                    (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCKNAME);
            lockStatic.setReferenceCounted(true);
        }

        return lockStatic;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "I die! I die!");
    }

    /* private functions */

    void BluetoothInit() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
            }
        }
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if ((mBluetoothAdapter==null) || (!mBluetoothAdapter.isEnabled())) {
            if (mBound == false) {
                // can't ask for permission yet.
            } else {
                sendBLERequestForAccess();
            }
        } else {
            needBluetoothPermission = false;
            initializeLeAdapter();
        }
    }

    public boolean initializeLeAdapter() {
        Log.d(TAG,"initializeLeAdapter: attempting to get an adapter");
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        } else if (!mBluetoothAdapter.isEnabled()) {
            // NOTE: This does not work!
            Log.e(TAG, "Bluetooth is not enabled.");
        }
        return true;
    }

    private boolean handleIPCMessage(Message m) {
        Bundle bundle = m.getData();
        String messageType = (String) bundle.getString(RT2Const.IPC.messageKey);
        if (messageType == null) {
            Log.e(TAG, "handleIPCMessage: missing messageType value");
            return false;
        }
        Log.d(TAG,"handleIPCMessage: " + messageType);
        if (RT2Const.IPC.MSG_BLE_accessGranted.equals(messageType)) {
            //initializeLeAdapter();
            //BluetoothInit();
            /*
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter!=null) {
                mDevice = (BluetoothClass.Device)intent.getSerializableExtra(BT_DEVICE);
            }
            */
        } else if (RT2Const.IPC.MSG_BLE_accessDenied.equals(messageType)) {
            stopSelf(); // This will stop the service.
        } else if (RT2Const.IPC.MSG_BLE_useThisDevice.equals(messageType)) {
            String deviceAddress = bundle.getString(RT2Const.IPC.MSG_BLE_useThisDevice_addressKey);
            if (deviceAddress == null) {
                Toast.makeText(mContext, "Null RL address passed", Toast.LENGTH_SHORT).show();
                Log.e(TAG,"handleIPCMessage: null RL address passed");
            } else {
                Toast.makeText(mContext, "Using RL " + deviceAddress, Toast.LENGTH_SHORT).show();
                Log.d(TAG,"handleIPCMessage: Using RL " + deviceAddress);
                if (mBluetoothAdapter == null) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                }
                if (mBluetoothAdapter != null) {
                    if (mBluetoothAdapter.isEnabled()) {
                        rileyLinkBLE.findRileyLink(deviceAddress);
                        // If successful, we will get a broadcast from RileyLinkBLE: RT2Const.serviceLocal.bluetooth_connected
                    } else {
                        Log.e(TAG, "Bluetooth is not enabled.");
                    }
                } else {
                    Log.e(TAG, "Failed to get adapter");
                }

            }
        } else if (messageType.equals(RT2Const.IPC.MSG_PUMP_tunePump)) {
            pumpManager.tunePump();
        } else if (messageType.equals(RT2Const.IPC.MSG_PUMP_fetchHistory)) {
            ArrayList<Page> pages = pumpManager.getAllHistoryPages();
        } else if (RT2Const.IPC.MSG_PUMP_useThisAddress.equals(messageType)) {
            String idString = bundle.getString("pumpID");
            if ((idString != null) && (idString.length()==6)) {
                setPumpIDString(idString);
            }
        } else {
            Log.e(TAG,"handleIPCMessage: unhandled message: " + messageType);
            return false;
        }
        return true;
    }

    private void setPumpIDString(String idString) {
        if (idString.length() != 6) {
            Log.e(TAG,"setPumpIDString: invalid pump id string: " + idString);
        }
        pumpIDString = idString;
        pumpIDBytes = ByteUtil.fromHexString(pumpIDString);
        SharedPreferences prefs = mContext.getSharedPreferences(RT2Const.serviceLocal.sharedPreferencesKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(RT2Const.serviceLocal.pumpIDKey,pumpIDString);
        editor.apply();
        Log.i(TAG,"setPumpIDString: saved pumpID "+pumpIDString);
    }

    private boolean sendMessage(Message msg) {
        if (!mBound) {
            Log.e(TAG,"sendMessage: not bound -- cannot send");
            return false;
        }
        if (mClients.isEmpty()) {
            Log.e(TAG,"sendMessage: cannot send, no clients!");
        } else {
            try {
                for (Messenger client : mClients) {
                    client.send(msg);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void sendMessage(String messageType) {
        if (!mBound) {
            Log.e(TAG,"sendMessage("+messageType+") cannot send message -- not yet bound");
        }
        Message msg = Message.obtain(null, RT2Const.IPC.MSG_IPC,0,0);
        // Create a bundle with the data
        Bundle bundle = new Bundle();
        bundle.putString(RT2Const.IPC.messageKey, messageType);

        // Set payload
        msg.setData(bundle);
        sendMessage(msg);
        Log.d(TAG,"sendMessage: sent "+messageType);
    }

    private void sendBLERequestForAccess() {
        sendMessage(RT2Const.IPC.MSG_BLE_requestAccess);
    }

    private void reportPumpFound() {
        sendMessage(RT2Const.IPC.MSG_PUMP_pumpFound);
    }
}

