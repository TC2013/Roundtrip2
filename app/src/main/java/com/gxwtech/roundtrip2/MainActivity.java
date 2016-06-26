package com.gxwtech.roundtrip2;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
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

import com.gxwtech.roundtrip2.HistoryActivity.HistoryPageListActivity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListView;


import com.gxwtech.roundtrip2.CommunicationService.CommunicationService;
import com.gxwtech.roundtrip2.RoundtripService.RoundtripService;
import com.gxwtech.roundtrip2.ServiceData.ServiceClientActions;
import com.gxwtech.roundtrip2.ServiceData.ServiceCommand;
import com.gxwtech.roundtrip2.RoundtripService.RoundtripServiceIPCFunctions;
import com.gxwtech.roundtrip2.util.tools;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 2177; // just something unique.
    private RoundtripServiceClientConnection roundtripServiceClientConnection;
    private BroadcastReceiver mBroadcastReceiver;
    private RoundtripServiceIPCFunctions clientConnection;

    BroadcastReceiver apsAppConnected;
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

        //Sets default Preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref_pump, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_rileylink, false);



        //UI RT Service client connection
        clientConnection = new RoundtripServiceIPCFunctions();

        setBroadcastReceiver();

        /* start the RoundtripService */

        /* using startService() will keep the service running until it is explicitly stopped
         * with stopService() or by RoundtripService calling stopSelf().
         * Note that calling startService repeatedly has no ill effects on RoundtripService
         */
        // explicitly call startService to keep it running even when the GUI goes away.
        Intent bindIntent = new Intent(this,RoundtripService.class);
        startService(bindIntent);
        // bind to the service for ease of message passing.
        //doBindService();

        //Make sure CommunicationService is running, as this maybe first run
        startService(new Intent(this, CommunicationService.class));
    }

    @Override
    protected void onResume(){
        super.onResume();

        setBroadcastReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (apsAppConnected != null){
            LocalBroadcastManager.getInstance(MainApp.instance()).unregisterReceiver(apsAppConnected);
        }
        if (mBroadcastReceiver != null){
            LocalBroadcastManager.getInstance(MainApp.instance()).unregisterReceiver(mBroadcastReceiver);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            RoundtripServiceIPCFunctions client = new RoundtripServiceIPCFunctions();

            if (resultCode == RESULT_OK) {
                // User allowed Bluetooth to turn on
                // Let the service know
                client.sendBLEaccessGranted();
            } else if (resultCode == RESULT_CANCELED) {
                // Error, or user said "NO"
                client.sendBLEaccessDenied();
                finish();
            }
        }
    }

    public void setBroadcastReceiver(){
        //Register this receiver for UI Updates
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent receivedIntent) {

                if (receivedIntent == null) {
                    Log.e(TAG,"onReceive: received null intent");
                } else {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainApp.instance());

                        if (RT2Const.local.INTENT_serviceConnected.equals(action)) {

                            sendPUMP_useThisDevice("518163");
                            sendBLEuseThisDevice("00:07:80:2D:9E:F4"); // for automated testing
                        } else if (RT2Const.IPC.MSG_BLE_RileyLinkReady.equals(action)) {
                    switch (receivedIntent.getAction()) {
                        case RT2Const.local.INTENT_NEW_rileylinkAddressKey:
                            clientConnection.sendBLEuseThisDevice(prefs.getString(RT2Const.serviceLocal.rileylinkAddressKey, ""));
                            break;
                        case RT2Const.local.INTENT_NEW_pumpIDKey:
                            clientConnection.sendPUMP_useThisDevice(prefs.getString(RT2Const.serviceLocal.pumpIDKey, ""));
                            break;
                        case RT2Const.IPC.MSG_BLE_RileyLinkReady:
                            setRileylinkStatusMessage("OK");
                            break;
                        case RT2Const.IPC.MSG_BLE_requestAccess:
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            break;
                        case RT2Const.IPC.MSG_PUMP_pumpFound:
                            setPumpStatusMessage("OK");
                            break;
                        case RT2Const.IPC.MSG_PUMP_pumpLost:
                            setPumpStatusMessage("Lost");
                            break;
                        case RT2Const.IPC.MSG_PUMP_reportedPumpModel:
                            Bundle bundle = receivedIntent.getBundleExtra(RT2Const.IPC.bundleKey);
                            String modelString = bundle.getString("model", "(unknown)");
                            setPumpStatusMessage(modelString);
                            break;
                        case RT2Const.IPC.MSG_PUMP_history:
                            Intent launchHistoryViewIntent = new Intent(context,HistoryPageListActivity.class);
                            storeForHistoryViewer = receivedIntent.getExtras().getBundle(RT2Const.IPC.bundleKey);
                            //startActivity(new Intent(context,HistoryPageListActivity.class));
                            // wait for history viwere to announce "ready"
                            break;
                        case RT2Const.local.INTENT_historyPageViewerReady:
                            Intent sendHistoryIntent = new Intent(RT2Const.local.INTENT_historyPageBundleIncoming);
                            sendHistoryIntent.putExtra(RT2Const.IPC.MSG_PUMP_history_key, storeForHistoryViewer);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendHistoryIntent);
                        } else if (RT2Const.IPC.MSG_ServiceResult.equals(action)) {
                            Log.i(TAG,"Received ServiceResult");
                        } else {
                            Log.e(TAG,"Unrecognized intent action: " + action);
                        }
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RT2Const.IPC.MSG_BLE_RileyLinkReady);
        intentFilter.addAction(RT2Const.IPC.MSG_BLE_requestAccess);
        intentFilter.addAction(RT2Const.IPC.MSG_PUMP_pumpFound);
        intentFilter.addAction(RT2Const.IPC.MSG_PUMP_pumpLost);
        intentFilter.addAction(RT2Const.IPC.MSG_PUMP_reportedPumpModel);
        intentFilter.addAction(RT2Const.IPC.MSG_PUMP_history);
        intentFilter.addAction(RT2Const.IPC.MSG_ServiceResult);
        intentFilter.addAction(RT2Const.local.INTENT_historyPageViewerReady);
        intentFilter.addAction(RT2Const.local.INTENT_NEW_rileylinkAddressKey);
        intentFilter.addAction(RT2Const.local.INTENT_NEW_pumpIDKey);


        LocalBroadcastManager.getInstance(MainApp.instance()).registerReceiver(mBroadcastReceiver, intentFilter);
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
        clientConnection.sendIPCMessage(RT2Const.IPC.MSG_PUMP_tunePump);
    }

    public void onFetchHistoryButtonClicked(View view) {
        clientConnection.sendIPCMessage(RT2Const.IPC.MSG_PUMP_fetchHistory);
    }

    public void onFetchSavedHistoryButtonClicked(View view) {
        clientConnection.sendIPCMessage(RT2Const.IPC.MSG_PUMP_fetchSavedHistory);
    }

    public void onReadPumpClockButtonClicked(View view) {
        ServiceCommand readPumpClockCommand = ServiceClientActions.makeReadPumpClockCommand();
        roundtripServiceClientConnection.sendServiceCommand(readPumpClockCommand);
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
        Drawable apsIcon                = getDrawable(R.drawable.refresh);

        logsIcon.setColorFilter(getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
        historyIcon.setColorFilter(getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
        settingsIcon.setColorFilter(getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
        catIcon.setColorFilter(getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
        apsIcon.setColorFilter(getResources().getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);

        ListView mDrawerList            = (ListView)findViewById(R.id.navList);
        ArrayList<NavItem> menuItems    = new ArrayList<>();
        menuItems.add(new NavItem("APS Integration", apsIcon));
        menuItems.add(new NavItem("Pump History", historyIcon));
        menuItems.add(new NavItem("Treatment Logs", logsIcon));
        menuItems.add(new NavItem("Settings", settingsIcon));
        menuItems.add(new NavItem("View LogCat", catIcon));
        DrawerListAdapter adapterMenu = new DrawerListAdapter(this, menuItems);
        mDrawerList.setAdapter(adapterMenu);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        //Check APS App Connectivity
                        sendAPSAppMessage(view);
                        break;
                    case 1:
                        //Pump History
                        startActivity(new Intent(getApplicationContext(), HistoryPageListActivity.class));
                        break;
                    case 2:
                        //Treatment Logs
                        startActivity(new Intent(getApplicationContext(), TreatmentHistory.class));
                        break;
                    case 3:
                        //Settings
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        break;
                    case 4:
                        //View LogCat
                        tools.showLogging();
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


    /* Functions for APS App Service */

    //Our Service that APS App will connect to
    private Messenger myService = null;
    private ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            myService = new Messenger(service);

            //Broadcast there has been a connection
            Intent intent = new Intent("APS_CONNECTED");
            LocalBroadcastManager.getInstance(MainApp.instance()).sendBroadcast(intent);
        }

        public void onServiceDisconnected(ComponentName className) {
            myService = null;
            //FYI, only called if Service crashed or was killed, not on unbind
        }
    };

    public void sendAPSAppMessage(final View view)
    {
        //listen out for a successful connection
        apsAppConnected = new BroadcastReceiver() {
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

                if (apsAppConnected != null) LocalBroadcastManager.getInstance(MainApp.instance()).unregisterReceiver(apsAppConnected); //Stop listening for new connections
                MainApp.instance().unbindService(myConnection);
            }
        };
        LocalBroadcastManager.getInstance(MainApp.instance()).registerReceiver(apsAppConnected, new IntentFilter("APS_CONNECTED"));

        connect_to_aps_app(MainApp.instance());
    }

    //Connect to the APS App Treatments Service
    private void connect_to_aps_app(Context c){
        // TODO: 16/06/2016 add user selected aps app
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