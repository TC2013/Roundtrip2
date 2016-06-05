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

/**
 * RoundtripService is intended to stay running when the gui-app is closed.
 */
public class RoundtripService extends Service {
    private static final String TAG="RoundtripService";
    private static final String WAKELOCKNAME = "com.gxwtech.roundtrip2.RoundtripServiceWakeLock";
    private static volatile PowerManager.WakeLock lockStatic = null;

    private Messenger mMessenger;
    private boolean mBound = false;
    private Handler mMessageHandler;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    // saved settings


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
                            Log.i(TAG, "Announcing RileyLink open For business");
                            sendBLEMessage(RT2Const.IPC.MSG_BLE_RileyLinkReady);
                            rfspy = new RFSpy(context, rileyLinkBLE);
                            pumpManager = new PumpManager(rfspy, new byte[]{0x51, (byte) 0x81, 0x63});
                            pumpManager.tunePump();

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

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, intentFilter);
        rileyLinkBLE = new RileyLinkBLE(this);
        Log.d(TAG, "onCreate(): I'm alive");
    }

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: Received message " + msg);
            Bundle bundle = msg.getData();
            switch(msg.what) {
                // This just helps sub-divide the message processing
                case RT2Const.IPC.MSG_BLE:
                    handleBLEMessage(msg);
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
            Log.d(TAG,"BluetoothInit: requesting Bluetooth access");
            sendBLERequestForAccess();
        } else {
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

    private void handleBLEMessage(Message m) {
        Bundle bundle = m.getData();
        String ble_message = (String)bundle.get("ble_message");
        if (ble_message == null) {
            Log.e(TAG,"handleBLEMessage: missing ble_message value");
            return;
        }
        if (RT2Const.IPC.MSG_BLE_accessGranted.equals(ble_message)) {
            //initializeLeAdapter();
            //BluetoothInit();
            /*
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter!=null) {
                mDevice = (BluetoothClass.Device)intent.getSerializableExtra(BT_DEVICE);
            }
            */
        } else if (RT2Const.IPC.MSG_BLE_accessDenied.equals(ble_message)) {
            stopSelf();
        } else if (RT2Const.IPC.MSG_BLE_useThisDevice.equals(ble_message)) {
            String deviceAddress = bundle.getString("address");
            if (deviceAddress == null) {
                Toast.makeText(mContext, "Null RL address passed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Using RL " + deviceAddress, Toast.LENGTH_SHORT).show();
                if (mBluetoothAdapter == null) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                }
                if (mBluetoothAdapter != null) {
                    if (mBluetoothAdapter.isEnabled()) {
                        rileyLinkBLE.findRileyLink(deviceAddress);
                        // If successful, we will get a broadcast from RileyLinkBLE: RT2Const.serviceLocal.bluetooth_connected
                    } else {
                        Log.e(TAG,"Bluetooth is not enabled.");
                    }
                } else {
                    Log.e(TAG,"Failed to get adapter");
                }

            }
        }

    }

    private boolean sendMessage(Bundle bundle) {
        if (!mBound) {
            Log.e(TAG,"sendMessage: not bound -- cannot send");
            return false;
        }

        // Create a Message
        Message msg = Message.obtain(null, RT2Const.IPC.MSG_BLE, 0, 0);

        // Set payload
        msg.setData(bundle);

        // Send the Message to the Service (in another process)
        try {
            mMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void sendBLEMessage(String bleMsgType) {
        // Create a bundle with the data
        Bundle bundle = new Bundle();
        bundle.putString("ble_message", bleMsgType);
        sendMessage(bundle);
        Log.d(TAG,"sendBLEMessage: sent "+bleMsgType);
    }

    private void sendBLERequestForAccess() {
        sendBLEMessage(RT2Const.IPC.MSG_BLE_requestAccess);
    }
}

