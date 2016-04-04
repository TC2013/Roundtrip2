package com.gxwtech.roundtrip2;

        import android.app.ListActivity;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothManager;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.content.SharedPreferences;
        import android.database.DataSetObserver;
        import android.os.Handler;
        import android.support.v4.content.LocalBroadcastManager;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.BaseAdapter;
        import android.widget.Button;
        import android.widget.ListAdapter;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.util.ArrayList;

public class DeviceScanActivity extends ListActivity {
    private static final String TAG="DeviceScanActivity";
    private LeDeviceListAdapter mLeDeviceListAdapter;

    private static final int STATE_startScan = 1;
    private static final int STATE_scanning = 2;
    private static final int STATE_noAddress = 3;
    private static final int STATE_haveAddress = 4;
    private int scanState = STATE_noAddress;

    private Handler mHandler;

    // For receiving and displaying log messages from the Service thread
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (RT2Const.INTENT_scanStarted.equals(intent.getAction())) {
                if (scanState == STATE_startScan) {
                    scanState = STATE_scanning;
                }
                refillFields();
            } else if (RT2Const.INTENT_scanStopped.equals(intent.getAction())) {
                if (scanState == STATE_scanning) {
                    scanState = STATE_noAddress;
                }
                refillFields();
            } else if (RT2Const.INTENT_deviceFound.equals(intent.getAction())) {
                String address = intent.getStringExtra("address");
                if (address == null) {
                    address = "(null)";
                }
                Log.i(TAG,"Received DeviceFound message, address="+address);
                mLeDeviceListAdapter.addDevice(address);
                mLeDeviceListAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mHandler = new Handler();
        this.setListAdapter(mLeDeviceListAdapter);
    }

    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RT2Const.INTENT_deviceFound);
        intentFilter.addAction(RT2Const.INTENT_scanStarted);
        intentFilter.addAction(RT2Const.INTENT_scanStopped);

        // register our desire to receive broadcasts from RoundtripService
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiver, intentFilter);
        refillFields();
    }

    protected void refillFields() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(RT2Const.RT2Preferences, 0);
        String currentRL = prefs.getString("Default RileyLink", "");
        if ((currentRL != null) && (!"".equals(currentRL))) {
            scanState = STATE_haveAddress;
        }
        Button button = (Button)findViewById(R.id.buttonScanOrForget);
        TextView tv = (TextView) findViewById(R.id.textViewCurrentRL);
        switch(scanState) {
            case STATE_noAddress: {
                button.setText("Scan");
                tv.setText("(None)");
            } break;
            case STATE_startScan: {
                button.setText("(Scan started)");
                tv.setText("(None)");
            } break;
            case STATE_scanning: {
                button.setText("Scanning...");
                tv.setText("(None)");
            } break;
            case STATE_haveAddress: {
                button.setText("Forget");
                if ((currentRL != null) && (!"".equals(currentRL))) {
                    tv.setText(currentRL);
                } else {
                    tv.setText("(error)");
                }
            }
        }
    }

    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(broadcastReceiver);
    }

    private class LeDeviceListAdapter extends BaseAdapter {
        public LeDeviceListAdapter() {
            this.items = new ArrayList<>();
        }

        private ArrayList<String> items;

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View V = view;
            if (V == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                V = vi.inflate(R.layout.ble_devlist_row,null);
            }
            String address = items.get(i);
            TextView name = (TextView)V.findViewById(R.id.ble_dev_name);
            name.setText(address);
            return V;
        }

        public void addDevice(String address) {
            items.add(address);
        }

        public void toggleItem(int i) {
            // NYI
        }

        public void removeAllItems() {
            items.clear();
        }
    }

    protected void scanTimeout() {
        // Uh, oh.  We asked the service to scan for RL devices,
        // and it took too long.  Try to recover.
        scanState = STATE_noAddress;
        refillFields();
    }

    public void onScanOrForgetButtonClicked(View view) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(RT2Const.RT2Preferences,0);
        String currentRL = prefs.getString("Default RileyLink", "");
        if (scanState == STATE_noAddress) {
//        if ((currentRL==null) || ("".equals(currentRL))) {
            // then scan
            // whack current listView contents
            mLeDeviceListAdapter.removeAllItems();
            mLeDeviceListAdapter.notifyDataSetChanged();
            // Tell RoundtripService to find BLE devices

            Intent intent = new Intent(RT2Const.INTENT_startScan);
            /* do not include a specific device to scan for...*/
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            Log.d(TAG, "Sent Intent_startScan");
            scanState = STATE_startScan;

            // set a timer, in case service never responds
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanTimeout();
                }
            },11000);

            refillFields();
        } else if (scanState == STATE_haveAddress) {
            // then forget
            prefs.edit().putString("Default RileyLink","").commit();
            scanState = STATE_noAddress;
            refillFields();
        }
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mLeDeviceListAdapter.toggleItem(position);
        String deviceAddress = (String)mLeDeviceListAdapter.getItem(position);
        // record the device chosen.
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(RT2Const.RT2Preferences,0);
        prefs.edit().putString("Default RileyLink", deviceAddress).commit();
        Toast.makeText(this, "New RileyLink: " + deviceAddress, Toast.LENGTH_SHORT).show();
        scanState = STATE_haveAddress;
        refillFields();
        // send device found
        Intent intent = new Intent(RT2Const.INTENT_deviceSelected).putExtra("address",deviceAddress);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        //finish();
    }

}
