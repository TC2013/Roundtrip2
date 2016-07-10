package com.gxwtech.roundtrip2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gxwtech.roundtrip2.HistoryActivity.HistoryPageListActivity;
import com.gxwtech.roundtrip2.RoundtripService.RoundtripService;
import com.gxwtech.roundtrip2.ServiceData.ReadPumpClockResult;
import com.gxwtech.roundtrip2.ServiceData.ServiceClientActions;
import com.gxwtech.roundtrip2.ServiceData.ServiceCommand;
import com.gxwtech.roundtrip2.ServiceData.ServiceNotification;
import com.gxwtech.roundtrip2.ServiceData.ServiceResult;
import com.gxwtech.roundtrip2.ServiceData.ServiceTransport;
import com.gxwtech.roundtrip2.ServiceMessageViewActivity.ServiceMessageViewListActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 2177; // just something unique.
    private RoundtripServiceClientConnection roundtripServiceClientConnection;
    private BroadcastReceiver mBroadcastReceiver;

    Bundle storeForHistoryViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w(TAG,"onCreate");
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
                            showIdle();

                            /*
                            ServiceCommand cmd = ServiceClientActions.makeSetPumpIDCommand("518163");
                            showBusy("Set Pump Address",1);
                            roundtripServiceClientConnection.sendServiceCommand(cmd);
                                    */

                            /**
                             * Client MUST send a "UseThisRileylink" message because it asserts that
                             * the user has given explicit permission to use bluetooth.
                             *
                             * We can change the format so that it is a simple "bluetooth OK" message,
                             * rather than an explicit address of a Rileylink, and the Service can
                             * use the last known good value.  But the kick-off of bluetooth ops must
                             * come from an Activity.
                             */
                            showBusy("Configuring Service",50);
                            roundtripServiceClientConnection.sendServiceCommand(
                                    ServiceClientActions.makeUseThisRileylinkCommand("00:07:80:2D:9E:F4"));
                        } else if (RT2Const.local.INTENT_historyPageViewerReady.equals(action)) {
                            Intent sendHistoryIntent = new Intent(RT2Const.local.INTENT_historyPageBundleIncoming);
                            sendHistoryIntent.putExtra(RT2Const.IPC.MSG_PUMP_history_key, storeForHistoryViewer);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendHistoryIntent);
                        } else if (RT2Const.IPC.MSG_ServiceResult.equals(action)) {
                            Log.i(TAG, "Received ServiceResult");

                            Bundle bundle = receivedIntent.getBundleExtra(RT2Const.IPC.bundleKey);
                            ServiceTransport transport = new ServiceTransport(bundle);
                            if (transport.commandDidCompleteOK()) {
                                String originalCommandName = transport.getOriginalCommandName();
                                if ("ReadPumpClock".equals(originalCommandName)) {
                                    ReadPumpClockResult clockResult = new ReadPumpClockResult();
                                    clockResult.initFromServiceResult(transport.getServiceResult());
                                    TextView pumpTimeTextView = (TextView) findViewById(R.id.textViewPumpClockTime);
                                    pumpTimeTextView.setText(clockResult.getTimeString());
                                    showIdle();
                                } else if ("RetrieveHistoryPage".equals(originalCommandName)) {
                                    Intent launchHistoryViewIntent = new Intent(context,HistoryPageListActivity.class);
                                    storeForHistoryViewer = receivedIntent.getExtras().getBundle(RT2Const.IPC.bundleKey);
                                    startActivity(new Intent(context,HistoryPageListActivity.class));
                                    // wait for history viewer to announce "ready"
                                    showIdle();
                                } else {
                                    Log.e(TAG,"Dunno what to do with this command completion: " + transport.getOriginalCommandName());
                                }
                            } else {
                                Log.e(TAG,"Command failed? " + transport.getOriginalCommandName());
                            }
                        } else if (RT2Const.IPC.MSG_ServiceNotification.equals(action)) {
                            ServiceTransport transport = new ServiceTransport(receivedIntent.getBundleExtra(RT2Const.IPC.bundleKey));
                            ServiceNotification notification = transport.getServiceNotification();
                            String note = notification.getNotificationType();
                            if (RT2Const.IPC.MSG_BLE_RileyLinkReady.equals(note)) {
                                setRileylinkStatusMessage("OK");
                            } else if (RT2Const.IPC.MSG_PUMP_pumpFound.equals(note)) {
                                setPumpStatusMessage("OK");
                            } else if (RT2Const.IPC.MSG_PUMP_pumpLost.equals(note)) {
                                setPumpStatusMessage("Lost");
                            } else if (RT2Const.IPC.MSG_note_WakingPump.equals(note)) {
                                showBusy("Waking Pump", 99);
                            } else if (RT2Const.IPC.MSG_note_FindingRileyLink.equals(note)) {
                                showBusy("Finding RileyLink", 99);
                            } else if (RT2Const.IPC.MSG_note_Idle.equals(note)) {
                                showIdle();
                            } else if (RT2Const.IPC.MSG_note_TaskProgress.equals(note)) {
                                int progress = notification.getMap().getInt("progress");
                                String taskName = notification.getMap().getString("task");
                                showBusy(taskName,progress);
                            } else {
                                Log.e(TAG,"Unrecognized Notification: '" + note + "'");
                            }
                        } else {
                            Log.e(TAG,"Unrecognized intent action: " + action);
                        }
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RT2Const.local.INTENT_serviceConnected);
        intentFilter.addAction(RT2Const.IPC.MSG_ServiceResult);
        intentFilter.addAction(RT2Const.IPC.MSG_ServiceNotification);
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

        linearProgressBar = (ProgressBar)findViewById(R.id.progressBarCommandActivity);
        spinnyProgressBar = (ProgressBar)findViewById(R.id.progressBarSpinny);
    }

    @Override
    protected void onDestroy() {
        Log.w(TAG,"bye-bye");
        doUnbindService();
        super.onDestroy();
    }

    /* Functions for sending messages to RoundtripService */

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



    /**
     *
     *  GUI element functions
     *
     */


    private int mProgress = 0;
    private int mSpinnyProgress = 0;
    private ProgressBar linearProgressBar;
    private ProgressBar spinnyProgressBar;
    private static final int spinnyFPS = 10;
    private Thread spinnyThread;
    void showBusy(String activityString, int progress) {
        mProgress = progress;
        TextView tv = (TextView)findViewById(R.id.textViewActivity);
        tv.setText(activityString);
        linearProgressBar.setProgress(progress);
        if (progress > 0) {
            spinnyProgressBar.setVisibility(View.VISIBLE);
            if (spinnyThread == null) {
                spinnyThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while ((mProgress > 0) && (mProgress < 100)) {
                            mSpinnyProgress += 100 / spinnyFPS;
                            spinnyProgressBar.setProgress(mSpinnyProgress);
                            SystemClock.sleep(1000 / spinnyFPS);
                        }
                        spinnyThread = null;
                    }
                });
                spinnyThread.start();
            }
        } else {
            spinnyProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    void showIdle() {
        showBusy("Idle",0);
    }

    void setRileylinkStatusMessage(String statusMessage) {
        TextView field = (TextView)findViewById(R.id.textViewFieldRileyLink);
        field.setText(statusMessage);
    }

    void setPumpStatusMessage(String statusMessage) {
        TextView field = (TextView)findViewById(R.id.textViewFieldPump);
        field.setText(statusMessage);
    }

    public void onTunePumpButtonClicked(View view) {
        //sendIPCMessage(RT2Const.IPC.MSG_PUMP_tunePump);
    }

    public void onFetchHistoryButtonClicked(View view) {
        /* does not work. Crashes sig 11
        showBusy("Fetch history page 0",50);
        ServiceCommand retrievePageCommand = ServiceClientActions.makeRetrieveHistoryPageCommand(0);
        roundtripServiceClientConnection.sendServiceCommand(retrievePageCommand);
        */
    }

    public void onFetchSavedHistoryButtonClicked(View view) {
        //sendIPCMessage(RT2Const.IPC.MSG_PUMP_fetchSavedHistory);
    }

    public void onReadPumpClockButtonClicked(View view) {
        showBusy("Reading Pump Clock",50);
        ServiceCommand readPumpClockCommand = ServiceClientActions.makeReadPumpClockCommand();
        roundtripServiceClientConnection.sendServiceCommand(readPumpClockCommand);
    }

    public void onGetISFProfileButtonClicked(View view) {
        ServiceCommand getISFProfileCommand = ServiceClientActions.makeReadISFProfileCommand();
        roundtripServiceClientConnection.sendServiceCommand(getISFProfileCommand);
    }

    public void onViewEventLogButtonClicked(View view) {
        startActivity(new Intent(getApplicationContext(),ServiceMessageViewListActivity.class));
    }

}
