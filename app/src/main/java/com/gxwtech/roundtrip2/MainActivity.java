package com.gxwtech.roundtrip2;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.gxwtech.roundtrip2.RoundtripService.RoundtripService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 2177; // just something unique.
    private Messenger mMessenger;
    private ServiceConnection mServiceConnection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

                /* start the RoundtripService */
        /* using startService() will keep the service running until it is explicitly stopped
         * with stopService() or by RoundtripService calling stopSelf().
         * Note that calling startService repeatedly has no ill effects on RoundtripService
         */
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG,"onServiceConnected");

                // Create the Messenger object
                mMessenger = new Messenger(iBinder);

                // Create a Message
                // Note the usage of MSG_SAY_HELLO as the what value
                /*
                Message msg = Message.obtain(null, RT2Const.MSG_ping, 0, 0);

                // Create a bundle with the data
                Bundle bundle = new Bundle();
                bundle.putString("key_hello", "world");

                // Set the bundle data to the Message
                msg.setData(bundle);

                // Send the Message to the Service (in another process)
                try {
                    mMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                */
                sendBLEuseThisDevice("00:07:80:2D:9E:F4"); // for automated testing
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        // explicitly call startService to keep it running even when the GUI goes away.
        Intent bindIntent = new Intent(this,RoundtripService.class);
        startService(bindIntent);
        // bind to the service for ease of message passing.
        bindService(bindIntent,mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    protected void startRoundtripService() {
        Log.d(TAG,"Sending intent to start service");
        startActivity(new Intent(this,RoundtripService.class));
    }
    /* for messages from RoundtripService */
    private void handleBLEMessage(Message m) {
        Bundle bundle = m.getData();
        String ble_message = (String)bundle.get("ble_message");
        if (ble_message == null) {
            Log.e(TAG,"handleBLEMessage: missing ble_message value");
            return;
        }
        if (RT2Const.IPC.MSG_BLE_requestAccess.equals(ble_message)) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else if (RT2Const.IPC.MSG_BLE_RileyLinkReady.equals(ble_message)) {
            Intent intent = new Intent(RT2Const.local.INTENT_RileyLinkReady);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            Log.e(TAG,"handleBLEMessage: failed to handle message "+ble_message);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // User allowed Bluetooth to turn on
                // Let the service know
                sendBLEaccessGranted();
            } else if (resultCode == RESULT_CANCELED) {
                // Error, or user said "NO"
                sendBLEaccessDenied();
                finish();
            }
        }
    }



    /* Functions for sending messages to RoundtripService */

    private void sendMessage(Bundle bundle) {
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
    }

    private void sendBLEMessage(String bleMsgType) {
        // Create a bundle with the data
        Bundle bundle = new Bundle();
        bundle.putString("ble_message", bleMsgType);
        sendMessage(bundle);
        Log.d(TAG,"sendBLEMessage: sent "+bleMsgType);
    }

    private void sendBLEaccessGranted() { sendBLEMessage(RT2Const.IPC.MSG_BLE_accessGranted); }

    private void sendBLEaccessDenied() { sendBLEMessage(RT2Const.IPC.MSG_BLE_accessDenied); }

    private void sendBLEuseThisDevice(String address) {
        Bundle bundle = new Bundle();
        bundle.putString("ble_message", RT2Const.IPC.MSG_BLE_useThisDevice);
        bundle.putString("address",address);
        sendMessage(bundle);
        Log.d(TAG,"sendBLEMessage: (use this address) "+address);
    }


}
