package com.gxwtech.roundtrip2;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gxwtech.roundtrip2.HistoryActivity.HistoryPageListActivity;
import com.gxwtech.roundtrip2.RoundtripService.RoundtripService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 2177; // just something unique.
    private RoundtripServiceClientConnection roundtripServiceClientConnection;
    private BroadcastReceiver mBroadcastReceiver;

    Bundle storeForHistoryViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        roundtripServiceClientConnection = new RoundtripServiceClientConnection(this);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent receivedIntent) {
                if (receivedIntent == null) {
                    Log.e(TAG,"onReceive: received null intent");
                } else {
                    String action = receivedIntent.getAction();
                    if (action == null) {
                        Log.e(TAG, "onReceive: null action");
                    } else {
                        Intent intent;

                        if (RT2Const.local.INTENT_serviceConnected.equals(action)) {

                            sendPUMP_useThisDevice("518163");
                            sendBLEuseThisDevice("00:07:80:2D:9E:F4"); // for automated testing
                        } else if (RT2Const.IPC.MSG_BLE_RileyLinkReady.equals(action)) {
                            setRileylinkStatusMessage("OK");
                        } else if (RT2Const.IPC.MSG_BLE_requestAccess.equals(action)) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        } else if (RT2Const.IPC.MSG_PUMP_pumpFound.equals(action)) {
                            setPumpStatusMessage("OK");
                        } else if (RT2Const.IPC.MSG_PUMP_pumpLost.equals(action)) {
                            setPumpStatusMessage("Lost");
                        } else if (RT2Const.IPC.MSG_PUMP_reportedPumpModel.equals(action)) {
                            Bundle bundle = receivedIntent.getBundleExtra(RT2Const.IPC.bundleKey);
                            String modelString = bundle.getString("model", "(unknown)");
                            setPumpStatusMessage(modelString);
                        } else if (RT2Const.IPC.MSG_PUMP_history.equals(action)) {
                            Intent launchHistoryViewIntent = new Intent(context,HistoryPageListActivity.class);
                            storeForHistoryViewer = receivedIntent.getExtras().getBundle(RT2Const.IPC.bundleKey);
                            startActivity(new Intent(context,HistoryPageListActivity.class));
                            // wait for history viwere to announce "ready"
                        } else if (RT2Const.local.INTENT_historyPageViewerReady.equals(action)) {
                            Intent sendHistoryIntent = new Intent(RT2Const.local.INTENT_historyPageBundleIncoming);
                            sendHistoryIntent.putExtra(RT2Const.IPC.MSG_PUMP_history_key,storeForHistoryViewer);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendHistoryIntent);
                        } else {
                            Log.e(TAG,"Unrecognized intent action: " + action);
                        }
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RT2Const.local.INTENT_serviceConnected);
        intentFilter.addAction(RT2Const.IPC.MSG_BLE_RileyLinkReady);
        intentFilter.addAction(RT2Const.IPC.MSG_BLE_requestAccess);
        intentFilter.addAction(RT2Const.IPC.MSG_PUMP_pumpFound);
        intentFilter.addAction(RT2Const.IPC.MSG_PUMP_pumpLost);
        intentFilter.addAction(RT2Const.IPC.MSG_PUMP_reportedPumpModel);
        intentFilter.addAction(RT2Const.IPC.MSG_PUMP_history);
        intentFilter.addAction(RT2Const.local.INTENT_historyPageViewerReady);

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);


        /* start the RoundtripService */
        /* using startService() will keep the service running until it is explicitly stopped
         * with stopService() or by RoundtripService calling stopSelf().
         * Note that calling startService repeatedly has no ill effects on RoundtripService
         */
        // explicitly call startService to keep it running even when the GUI goes away.
        Intent bindIntent = new Intent(this,RoundtripService.class);
        startService(bindIntent);
        // bind to the service for ease of message passing.
        doBindService();
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

    private boolean sendMessage(Bundle bundle) {
        return roundtripServiceClientConnection.sendMessage(bundle);
    }

    /* Functions for sending messages to RoundtripService */

    // send one-liner message to RoundtripService
    private void sendIPCMessage(String ipcMsgType) {
        // Create a bundle with the data
        Bundle bundle = new Bundle();
        bundle.putString(RT2Const.IPC.messageKey, ipcMsgType);
        if (sendMessage(bundle)) {
            Log.d(TAG,"sendIPCMessage: sent "+ipcMsgType);
        } else {
            Log.e(TAG,"sendIPCMessage: send failed");
        }
    }

    private void sendBLEaccessGranted() { sendIPCMessage(RT2Const.IPC.MSG_BLE_accessGranted); }

    private void sendBLEaccessDenied() { sendIPCMessage(RT2Const.IPC.MSG_BLE_accessDenied); }

    private void sendBLEuseThisDevice(String address) {
        Bundle bundle = new Bundle();
        bundle.putString(RT2Const.IPC.messageKey, RT2Const.IPC.MSG_BLE_useThisDevice);
        bundle.putString(RT2Const.IPC.MSG_BLE_useThisDevice_addressKey,address);
        sendMessage(bundle);
        Log.d(TAG,"sendIPCMessage: (use this address) "+address);
    }


    private void sendPUMP_useThisDevice(String pumpIDString) {
        Bundle bundle = new Bundle();
        bundle.putString(RT2Const.IPC.messageKey, RT2Const.IPC.MSG_PUMP_useThisAddress);
        bundle.putString(RT2Const.IPC.MSG_PUMP_useThisAddress_pumpIDKey,pumpIDString);
        sendMessage(bundle);
        Log.d(TAG,"sendPUMP_useThisDevice: " + pumpIDString);
    }

    public void doBindService() {
        bindService(new Intent(this,RoundtripService.class),
                roundtripServiceClientConnection.getServiceConnection(),
                Context.BIND_AUTO_CREATE);
        Log.d(TAG,"doBindService: binding.");
    }

    public void doUnbindService() {
        ServiceConnection conn = roundtripServiceClientConnection.getServiceConnection();
        roundtripServiceClientConnection.unbind();
        unbindService(conn);
        Log.d(TAG,"doUnbindService: unbinding.");
    }

    /*
    *
    *  GUI element functions
    *
     */

    void setRileylinkStatusMessage(String statusMessage) {
        TextView field = (TextView)findViewById(R.id.textViewFieldRileyLink);
        field.setText(statusMessage);
    }

    void setPumpStatusMessage(String statusMessage) {
        TextView field = (TextView)findViewById(R.id.textViewFieldPump);
        field.setText(statusMessage);
    }

    public void onTunePumpButtonClicked(View view) {
        sendIPCMessage(RT2Const.IPC.MSG_PUMP_tunePump);
    }

    public void onFetchHistoryButtonClicked(View view) {
        sendIPCMessage(RT2Const.IPC.MSG_PUMP_fetchHistory);
    }

    public void onFetchSavedHistoryButtonClicked(View view) {
        sendIPCMessage(RT2Const.IPC.MSG_PUMP_fetchSavedHistory);
    }

    public void onQuickTuneButtonClicked(View view) {
        sendIPCMessage(RT2Const.IPC.MSG_PUMP_quickTune);
    }

}
