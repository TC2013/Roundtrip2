package com.gxwtech.roundtrip2;

import android.bluetooth.BluetoothGatt;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private static final String TAG="MainActivity";
    BluetoothAdapter mBluetoothAdapter; // represents Android device's bluetooth adapter


    private final static int REQUEST_ENABLE_BT = 1;
    private SharedPreferences.OnSharedPreferenceChangeListener mOSPCL;
    private boolean isScanning = false;
    private ServiceConnection mServiceConnection;
    private boolean isBound = false;
    private Messenger mMessenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!isBLEFeatureAvailable()) {
            // no bluetooth? forget this.
            finish();
        }

        /*
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
        /* start the RoundtripService */
        /* using startService() will keep the service running until it is explicitly stopped
         * with stopService() or by RoundtripService calling stopSelf().
         * Note that calling startService repeatedly has no ill effects on RoundtripService
         */
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG,"onServiceConnected");
                isBound = true;

                // NEW: use the mRoundtripService for calls ?
                //mRoundtripService = new RoundtripServiceBinder(iBinder);

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

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };



        // explicitly call startService to keep it running even when the GUI goes away.
        Intent bindIntent = new Intent(this,RoundtripService.class);
        startService(bindIntent);
        // bind to the service for ease of message passing.
        bindService(bindIntent,mServiceConnection,Context.BIND_AUTO_CREATE);


        mOSPCL = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                if ("Default RileyLink".equals(s)) {
                    updateRLStatusButton(isScanning);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RT2Const.INTENT_startScan);
        intentFilter.addAction(RT2Const.INTENT_scanStarted);
        intentFilter.addAction(RT2Const.INTENT_scanStopped);
        intentFilter.addAction(RT2Const.INTENT_deviceSelected);

        // register our desire to receive broadcasts from RoundtripService
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiver, intentFilter);

    }




    public void updateRLStatusButton(boolean isScanning) {
        String defaultRLAddress = getSharedPreferences(RT2Const.RT2Preferences,0).getString("Default RileyLink",null);
        ImageButton ib = (ImageButton)findViewById(R.id.imageButtonRLStatus);
        try {
            if ((defaultRLAddress == null) || ("".equals(defaultRLAddress))) {
                if (isScanning) {
                    Log.d(TAG,"Set button to scanning");
                    ib.setImageResource(getResources().getIdentifier("ic_rl_s_mdpi", "drawable", getPackageName()));
                    //fab.setImageLevel(2);
                } else {
                    Log.d(TAG,"Set button to NotFound");
                    ib.setImageResource(getResources().getIdentifier("ic_rl_nf_mdpi","drawable",getPackageName()));
                }
            } else {
                Log.d(TAG, "Set button to Found");
                ib.setImageResource(getResources().getIdentifier("ic_rl_f_mdpi", "drawable", getPackageName()));
            }
        } catch (NullPointerException e) {
            Log.e(TAG,"Failed to load drawable resource.");
            e.printStackTrace();
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG,"onReceive: received intent with action "+action);
            if (RT2Const.INTENT_scanStarted.equals(action)) {
                isScanning = true;
                updateRLStatusButton(isScanning);
            } else if (RT2Const.INTENT_scanStopped.equals(action)) {
                isScanning = false;
                updateRLStatusButton(isScanning);
            } else if (RT2Const.INTENT_startScan.equals(action)) {
                // This is a request from some activity in the local app to tell
                // RoundtripService to start scanning for BLE devices
                sendBLEstartScan();
            } else if (RT2Const.INTENT_deviceSelected.equals(action)) {
                sendBLEuseThisDevice("00:07:80:39:4C:B1");
            }

        }
    };

    public void onResume() {
        super.onResume();
        getSharedPreferences(RT2Const.RT2Preferences,0).registerOnSharedPreferenceChangeListener(mOSPCL);
        updateRLStatusButton(isScanning);
    }

    public void onPause() {
        super.onPause();
        getSharedPreferences(RT2Const.RT2Preferences, 0).unregisterOnSharedPreferenceChangeListener(mOSPCL);
    }


    protected boolean isBLEFeatureAvailable() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void onRLScanButtonClicked(View view) {
        /* Launch the deviceScan activity */
        /*
        Intent intent = new Intent(this, DeviceScanActivity.class);
        this.startActivity(intent);
        */

        /*
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = adapter.getRemoteDevice("00:07:80:39:4c:b1");
        BluetoothGatt gatt = device.connectGatt(this,false,cb);
        */
        //gatt = device.connectGatt(ctx, false, mGattCallback);

        sendBLEuseThisDevice("00:07:80:39:4C:B1");
    }

    public void onTESTButtonClicked(View view) {
        sendBLETEST();
    }

    /* MessageHandler for receiving messages from RoundtripService */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: Received message " + msg);
            Bundle bundle = msg.getData();
            switch(msg.what) {
                case RT2Const.MSG_BLE:
                    handleBLEMessage(msg);
                    break;
                default:
                    Log.e(TAG,"handleMessage: unknown 'what' in message: "+msg.what);
                    super.handleMessage(msg);
            }
        }
    }

    /* for messages from RoundtripService */
    private void handleBLEMessage(Message m) {
        Bundle bundle = m.getData();
        String ble_message = (String)bundle.get("ble_message");
        if (ble_message == null) {
            Log.e(TAG,"handleBLEMessage: missing ble_message value");
            return;
        }
        if (RT2Const.MSG_BLE_deviceFound.equals(ble_message)) {
            Log.d(TAG, "handleBLEMessage: received MSG_BLE_deviceFound");
            // Announce this locally to all activities
            final String deviceAddress = (String) bundle.get("address");
            if (deviceAddress == null) {
                /* no param given... bad... */
                Log.e(TAG, "handleBLEMessage: deviceFound message contains no address");
            } else {
                Intent intent = new Intent(RT2Const.INTENT_deviceFound).putExtra("address", deviceAddress);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        } else if (RT2Const.MSG_BLE_requestForAccess.equals(ble_message)) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
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

    private void sendBLEstartScan() {
        sendBLEMessage(RT2Const.MSG_BLE_startScan);
    }

    private void sendBLEaccessGranted() { sendBLEMessage(RT2Const.MSG_BLE_accessGranted); }

    private void sendBLEaccessDenied() { sendBLEMessage(RT2Const.MSG_BLE_accessDenied); }

    private void sendBLEuseThisDevice(String address) {
        Bundle bundle = new Bundle();
        bundle.putString("ble_message", RT2Const.MSG_BLE_useThisDevice);
        bundle.putString("address",address);
        sendMessage(bundle);
        Log.d(TAG,"sendBLEMessage: (use this address) "+address);
    }

    private void sendBLETEST() { sendBLEMessage(RT2Const.MSG_BLE_TEST);}

}
