package com.gxwtech.roundtrip2;

import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.gxwtech.roundtrip2.rileylink.RileyLinkPacket;
import com.gxwtech.roundtrip2.rileylink.RileyLink;
import com.gxwtech.roundtrip2.rileylink.RileyLinkUtil;
import com.gxwtech.roundtrip2.util.ByteUtil;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RoundtripService extends Service {
    private static final String TAG="RoundtripService";
    private static final int DEFAULT_SEMAPHORE_WAITTIME_MS = 2000;
    private static final String WAKELOCKNAME = "com.gxwtech.roundtrip2.RoundtripServiceWakeLock";
    private static volatile PowerManager.WakeLock lockStatic = null;

        /*
    *
    * This section is for intents used for local broadcast between RoundtripService components
    *
     */

    public static final String LOCAL_BLE_startScan = "LOCAL_BLE_startScan";
    public static final String LOCAL_BLE_scanStarted = "LOCAL_BLE_scanStarted";
    public static final String LOCAL_BLE_scanStopped = "LOCAL_BLE_scanStopped";

    /* LOCAL_BLE_deviceFound has String parameter "address" which is the MAC address of the device */
    public static final String LOCAL_BLE_deviceFound = "LOCAL_BLE_deviceFound";



    // PRIVATE names for actions!
    private static final String ACTION_discoverDevice = "com.gxwtech.roundtrip2.action.discoverDevice";

    private Messenger mMessenger;
    private Handler mMessageHandler;
    private RileyLink mRileyLink;
    private Minimed mPump;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;

    public RoundtripService() {
        super();
        Log.d(TAG, "RoundtripService newly constructed");
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mContext = getApplicationContext();
        mRileyLink = new RileyLink(this); // constructed, but not yet useful.
        mPump = new Minimed();
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
                        if (LOCAL_BLE_deviceFound.equals(intent.getAction())) {
                            // the BLEDeviceScanner has found something.
                            // FIXME: For the moment, assume this is the RileyLink
                            String address = intent.getStringExtra("address");

                            //handleActionDiscoverDevice(address);
                            Log.e(TAG,"onReceive: LOCAL_BLE_deviceFound message received (ignoring)");
                        } else if (LOCAL_BLE_scanStarted.equals(intent.getAction())) {
                            // the BLEDeviceScanner is informing us the scan was started.
                            // Pass the message to the App
                            sendBLEScanStarted();
                        }
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LOCAL_BLE_deviceFound);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: Received message " + msg);
            Bundle bundle = msg.getData();
            switch(msg.what) {
                case RT2Const.MSG_ping:
                    String hello = (String)bundle.get("key_hello");
                    Toast.makeText(mContext,hello, Toast.LENGTH_SHORT).show();
                    break;
                case RT2Const.MSG_BLE:
                    handleBLEMessage(msg);
                    break;
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
        if (mRileyLink != null) {
            mRileyLink.close();
        }
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
            // NOTE: THIS DOES NOT WORK!
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
        if (RT2Const.MSG_BLE_startScan.equals(ble_message)) {
            Log.d(TAG, "handleBLEMessage: received startScan (ignoring)");
        } else if (RT2Const.MSG_BLE_accessGranted.equals(ble_message)) {
            //initializeLeAdapter();
            //BluetoothInit();
            /*
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter!=null) {
                mDevice = (BluetoothClass.Device)intent.getSerializableExtra(BT_DEVICE);
            }
            */
        } else if (RT2Const.MSG_BLE_accessDenied.equals(ble_message)) {
            stopSelf();
        } else if (RT2Const.MSG_BLE_useThisDevice.equals(ble_message)) {
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
                        mDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                        if (mDevice != null) {
                            mRileyLink = new RileyLink(getApplicationContext());
                            mRileyLink.init(mDevice);
                            mPump.setRileyLink(mRileyLink);
                        } else {
                            Log.e(TAG, "Failed to get device " + deviceAddress);
                        }
                    } else {
                        Log.e(TAG,"Bluetooth is not enabled.");
                    }
                } else {
                    Log.e(TAG,"Failed to get adapter");
                }

            }
        } else if (RT2Const.MSG_BLE_TEST.equals(ble_message)) {
            // do what?
            //mRileyLink.test();
            runTest();
        }

    }

    private void runTest() {
        //mPump.wakeup(5);
        //mPump.pressButton();

        // See if the RileyLink is listening
        mRileyLink.writeToData(new byte[] {RileyLinkPacket.RILEYLINK_CMD_GET_VERSION});
        byte[] response = mRileyLink.readFromData(500);
        if (response!=null) {
            Log.d(TAG,"Read from Rileylink: "+ByteUtil.shortHexString(response));
            Log.d(TAG, "RileyLink says: "+ByteUtil.showPrintable(response));
        } else {
            Log.d(TAG,"No data from Rileylink (timeout)");
        }



        for (byte sendChannel = 0; sendChannel < 5; sendChannel++) {
            for (byte recvChannel = 0; recvChannel < 5; recvChannel++) {
                // try simple wakey
                Log.e(TAG,String.format("TESTING SEND %d RECEIVE %d",sendChannel,recvChannel));
                byte[] cmd = new byte[]{RileyLinkPacket.RILEYLINK_CMD_SEND_AND_LISTEN, sendChannel, 0, 0, recvChannel, 1, 0, 3};
                byte[] wakey = new byte[]{(byte) 0xA7, (byte) 0x51, (byte) 0x81, (byte) 0x63, (byte) 0x5D, (byte) 0x00};
                byte[] full = ByteUtil.concat(cmd, RileyLinkUtil.encodeData(RileyLinkUtil.appendChecksum(wakey)));
                mRileyLink.writeToData(full);
                response = mRileyLink.readFromData(5000);
                if (response != null) {
                    Log.d(TAG, "Read from Rileylink: " + ByteUtil.shortHexString(response));
                    Log.d(TAG, "RileyLink says: "+ByteUtil.showPrintable(response));
                } else {
                    Log.d(TAG, "No data from Rileylink (timeout)");
                }

                // try full wakey
                byte[] fullWakeyCmd = new byte[]{RileyLinkPacket.RILEYLINK_CMD_SEND_AND_LISTEN, sendChannel, (byte) 200, 1, recvChannel, 2, 0, 3};
                String wakeupString = "A75181635D0201050000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
                byte[] fullWakey = ByteUtil.concat(cmd, ByteUtil.fromHexString(wakeupString));
                mRileyLink.writeToData(fullWakey);
                response = mRileyLink.readFromData(5000);
                if (response != null) {
                    Log.d(TAG, "Read from Rileylink: " + ByteUtil.shortHexString(response));
                    Log.d(TAG, "RileyLink says: "+ByteUtil.showPrintable(response));
                } else {
                    Log.d(TAG, "No data from Rileylink (timeout)");
                }
            }
        }

    }

    private void sendMessage(Bundle bundle) {
        // Create a Message
        Message msg = Message.obtain(null, RT2Const.MSG_BLE, 0, 0);

        // Set payload
        msg.setData(bundle);

        // Send the Message to the Service (in another process)
        try {
            mMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendBLEMessage(String bleMsgType) {
        // Create a bundle with the data
        Bundle bundle = new Bundle();
        bundle.putString("ble_message", bleMsgType);
        sendMessage(bundle);
        Log.d(TAG,"sendBLEMessage: sent "+bleMsgType);
    }

    private void sendBLEScanStarted() {
        sendBLEMessage(RT2Const.MSG_BLE_scanStarted);
    }

    private void sendBLERequestForAccess() { sendBLEMessage(RT2Const.MSG_BLE_requestForAccess); }
}

