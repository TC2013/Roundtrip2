package com.gxwtech.roundtrip2;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;


import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.gxwtech.roundtrip2.HappService.HappIncomingService;
import com.gxwtech.roundtrip2.HappService.Objects.Basal;
import com.gxwtech.roundtrip2.HappService.Objects.Treatment;
import com.gxwtech.roundtrip2.RoundtripService.RoundtripService;
import com.gxwtech.roundtrip2.util.tools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 2177; // just something unique.
    private RoundtripServiceClientConnection roundtripServiceClientConnection;
    private BroadcastReceiver mBroadcastReceiver;

    BroadcastReceiver happConnected;

    //used by HAPP Service to get app instance
    Bundle storeForHistoryViewer;

    //UI items
    private DrawerLayout mDrawerLayout;
    private LinearLayout mDrawerLinear;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupMenuAndToolbar();

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

        //Make sure HAPP BackgroundService is running, as this maybe first run
        startService(new Intent(this, HappIncomingService.class));
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

    @Override
    public void onPause() {
        super.onPause();
        if (happConnected != null){
            LocalBroadcastManager.getInstance(MainApp.instance()).unregisterReceiver(happConnected);
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

    /* UI Setup */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)){
                    mDrawerLayout.closeDrawers();
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(mDrawerLinear);
                return true;

            default:
                return true;
        }
    }

    public void setupMenuAndToolbar() {
        //Setup menu
        mDrawerLayout                   = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerLinear                   = (LinearLayout) findViewById(R.id.left_drawer);
        toolbar                         = (Toolbar) findViewById(R.id.mainActivityToolbar);
        Drawable logsIcon               = getDrawable(R.drawable.file_chart);
        Drawable historyIcon            = getDrawable(R.drawable.history);
        Drawable settingsIcon           = getDrawable(R.drawable.settings);
        Drawable catIcon                = getDrawable(R.drawable.cat);
        Drawable happIcon                = getDrawable(R.drawable.refresh);

        logsIcon.setColorFilter(getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
        historyIcon.setColorFilter(getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
        settingsIcon.setColorFilter(getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
        catIcon.setColorFilter(getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
        happIcon.setColorFilter(getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);

        ListView mDrawerList            = (ListView)findViewById(R.id.navList);
        ArrayList<NavItem> menuItems    = new ArrayList<>();
        menuItems.add(new NavItem("Pump History", historyIcon));
        menuItems.add(new NavItem("Treatment Logs", logsIcon));
        menuItems.add(new NavItem("Settings", settingsIcon));
        menuItems.add(new NavItem("View LogCat", catIcon));
        menuItems.add(new NavItem("Check HAPP Connectivity", happIcon));
        DrawerListAdapter adapterMenu = new DrawerListAdapter(this, menuItems);
        mDrawerList.setAdapter(adapterMenu);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        //Pump History
                        sendIPCMessage(RT2Const.IPC.MSG_PUMP_fetchHistory);
                        break;
                    case 1:
                        //Treatment Logs
                        startActivity(new Intent(getApplicationContext(), TreatmentHistory.class));
                        break;
                    case 2:
                        //Settings
                        //startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        break;
                    case 3:
                        //View LogCat
                        tools.showLogging();
                        break;
                    case 4:
                        //Check HAPP Connectivity
                        sendHappMessage(view);
                        break;
                }
                mDrawerLayout.closeDrawers();
            }
        });

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()

                //Insulin Integration App, try and connect
                //checkInsulinAppIntegration(false);
            }
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }


    /* Functions for Happ Service */

    //Our Service that HAPP will connect to
    private Messenger myService = null;
    private ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            myService = new Messenger(service);

            //Broadcast there has been a connection
            Intent intent = new Intent("HAPP_CONNECTED");
            //LocalBroadcastManager.getInstance(MainApp.instance()).sendBroadcast(intent);
            LocalBroadcastManager.getInstance(MainApp.instance()).sendBroadcast(intent); // TODO: 07/06/2016 ok?
        }

        public void onServiceDisconnected(ComponentName className) {
            myService = null;
            //FYI, only called if Service crashed or was killed, not on unbind
        }
    };

    public void sendHappMessage(final View view)
    {
        //listen out for a successful connection
        happConnected = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Resources appR = view.getContext().getResources();
                CharSequence txt = appR.getText(appR.getIdentifier("app_name", "string", view.getContext().getPackageName()));

                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString("ACTION","TEST_MSG");
                bundle.putString("UPDATE", txt.toString());
                msg.setData(bundle);

                try {
                    myService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    //cannot Bind to service
                    Snackbar snackbar = Snackbar
                            .make(view, "error sending msg: " + e.getMessage(), Snackbar.LENGTH_INDEFINITE);
                    snackbar.show();
                }

                if (happConnected != null) LocalBroadcastManager.getInstance(MainApp.instance()).unregisterReceiver(happConnected); //Stop listening for new connections
                MainApp.instance().unbindService(myConnection);
            }
        };
        LocalBroadcastManager.getInstance(MainApp.instance()).registerReceiver(happConnected, new IntentFilter("HAPP_CONNECTED"));

        connect_to_HAPP(MainApp.instance());
    }

    //Connect to the HAPP Treatments Service
    private void connect_to_HAPP(Context c){
        Intent intent = new Intent("com.hypodiabetic.happ.services.TreatmentService");
        intent.setPackage("com.hypodiabetic.happ");
        c.bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
    }



}

class NavItem {
    String mTitle;
    Drawable mIcon;

    public NavItem(String title, Drawable icon) {
        mTitle = title;
        mIcon = icon;
    }
}

class DrawerListAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<NavItem> mNavItems;

    public DrawerListAdapter(Context context, ArrayList<NavItem> navItems) {
        mContext = context;
        mNavItems = navItems;
    }

    @Override
    public int getCount() {
        return mNavItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mNavItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.menu_item, null);
        }
        else {
            view = convertView;
        }

        TextView titleView = (TextView) view.findViewById(R.id.menuText);
        ImageView iconView = (ImageView) view.findViewById(R.id.menuIcon);

        titleView.setText( mNavItems.get(position).mTitle);
        iconView.setBackground(mNavItems.get(position).mIcon);
        return view;
    }
}