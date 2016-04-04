package com.gxwtech.roundtrip2.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;

import com.gxwtech.roundtrip2.RoundtripService;
import com.gxwtech.roundtrip2.util.ByteUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by geoff on 3/19/16.
 *
 * NOTE: ACCESS_COARSE_LOCATION is required to scan for bluetooth devices
 *
 * Here's the results from a ScanRecord of a RileyLink (running old firmware?)
 *
 * LE Scan Result(00:07:80:39:4C:B1): ScanResult{mDevice=00:07:80:39:4C:B1, mScanRecord=ScanRecord [mAdvertiseFlags=6, mServiceUuids=[d39f1890-17eb-11e4-8c21-0800200c9a66], mManufacturerSpecificData={}, mServiceData={}, mTxPowerLevel=-2147483648, mDeviceName=], mRssi=-53, mTimestampNanos=1024213357992}
 * Scan callback: found device 00:07:80:39:4C:B1
 * ScanRecord info: ScanRecord [mAdvertiseFlags=6, mServiceUuids=[d39f1890-17eb-11e4-8c21-0800200c9a66], mManufacturerSpecificData={}, mServiceData={}, mTxPowerLevel=-2147483648, mDeviceName=]
 * ScanRecord device name:
 *
 * Here's a scan after I programmed both the CC1110 and BLE113 with rfspy code (3/21/16):
 *
 * LE Scan Result(00:07:80:39:4C:B1): ScanResult{mDevice=00:07:80:39:4C:B1, mScanRecord=ScanRecord [mAdvertiseFlags=6, mServiceUuids=[0235733b-99c5-4197-b856-69219c2a3845], mManufacturerSpecificData={}, mServiceData={}, mTxPowerLevel=-2147483648, mDeviceName=], mRssi=-73, mTimestampNanos=3659638567511}
 * Scan callback: found device 00:07:80:39:4C:B1
 * ScanRecord info: ScanRecord [mAdvertiseFlags=6, mServiceUuids=[0235733b-99c5-4197-b856-69219c2a3845], mManufacturerSpecificData={}, mServiceData={}, mTxPowerLevel=-2147483648, mDeviceName=]
 * ScanRecord device name:
 */
public class BLEDeviceScanner {
    private static final String TAG="BLEDeviceScanner";
    private Context ctx;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private String targetDeviceAddress = null;
    private Map<String,BluetoothDevice> deviceHashMap;

    public BLEDeviceScanner(Context context) {
        ctx = context;
        deviceHashMap = new HashMap<>();
        final BluetoothManager mBluetoothManager =
                (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    public int startScan() {
        // scan for all devices
        return startScan(null,false);
    }

    public int startScan(String deviceAddress) {
        return startScan(deviceAddress,false);
    }

    public int startScan(String deviceAddress, boolean rescanning) {

        Log.d(TAG,"startScan: entry");
        // if deviceAddress is null, scan for all.
        // else scan for one.
        // if rescanning is true, just check for "MATCH_LOST"
        List<ScanFilter> filters = new ArrayList<>();
        ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
        // api 23 adds some nice tools here... but I'm aiming for api 22
        ScanSettings scanSettings = settingsBuilder.build();

        /* scan filter: can check by remote MAC address, UUIDs identifying gatt services,
        *  Service data, and manufacturer specific data */
        List<ScanFilter> filterList = new ArrayList<>();
        ScanFilter.Builder filterBuilder = new ScanFilter.Builder();
        if (deviceAddress != null) {
            filterBuilder.setDeviceAddress(deviceAddress);
        }
        filterList.add(filterBuilder.build());
        /* TODO: Add filters for RileyLink specific devices */
        if (mBluetoothLeScanner == null) {
            Log.e(TAG,"mBluetoothLeScanner is NULL!");
            return 1;
        } else {
            mBluetoothLeScanner.startScan(filterList, scanSettings, mScanCallback);
        }
        return 0; // OK
    }

    // Device scan callback.
    private void handleDiscoveredBLEDevice(ScanResult result) {
        int i;
        Log.i(TAG, "LE Scan Result("+result.getDevice().getAddress()+"): "+result.toString());
        BluetoothDevice device = result.getDevice();
        Log.i(TAG, "Scan callback: found device " + device.getAddress());
        // cache the device locally
        deviceHashMap.put(device.getAddress(),device);
        Intent intent = new Intent(RoundtripService.LOCAL_BLE_deviceFound);
        intent.putExtra("address", device.getAddress());
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
        if (targetDeviceAddress!=null) {
            if (device.getAddress().equals(targetDeviceAddress)) {
                // found the one we were looking for. stop scanning.
                mBluetoothLeScanner.stopScan(mScanCallback);
            }
        }
        /* get detailed information, so we can make a better filter */
        ScanRecord record = result.getScanRecord();
        if (record==null) {
            Log.d(TAG, "ScanRecord info: record is null");
            return;
        } else {
            Log.d(TAG, "ScanRecord info: " + record.toString());
        }
        SparseArray<byte[]> manufacturerSpecificData = record.getManufacturerSpecificData();
        for (i = 0; i<manufacturerSpecificData.size(); i++) {
            Log.d(TAG, "ScanRecord manuf("+ i +"): " + ByteUtil.shortHexString(manufacturerSpecificData.get(i)));
        }
        Log.d(TAG,"ScanRecord device name: " + record.getDeviceName());
        Map<ParcelUuid,byte[]> pbMap = record.getServiceData();
        Iterator iterator = pbMap.keySet().iterator();
        for (ParcelUuid key : pbMap.keySet()) {
            byte[] value = pbMap.get(key);
            Log.d(TAG,"ScanRecord svc uuid " + key.toString() + ":" + ByteUtil.shortHexString(value));
        }
    }

    /* http://developer.android.com/reference/android/bluetooth/le/ScanCallback.html */
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            /* API 23 stuff:
            switch (callbackType) {
                case ScanSettings.CALLBACK_TYPE_ALL_MATCHES:
                    Log.i(TAG, "callbackType: ALL_MATCHES");
                    break;
                case ScanSettings.CALLBACK_TYPE_FIRST_MATCH:
                    Log.i(TAG, "callbackType: FIRST_MATCH");
                    break;
                case ScanSettings.CALLBACK_TYPE_MATCH_LOST:
                    Log.i(TAG, "callbackType: MATCH_LOST");
                    break;
                default:
                    Log.e(TAG, "callbackType: unknown code");
                    break;
            }
            */
            handleDiscoveredBLEDevice(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                handleDiscoveredBLEDevice(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
                    /* int 	SCAN_FAILED_ALREADY_STARTED 	Fails to start scan as BLE scan with the same settings is already started by the app.
                       int 	SCAN_FAILED_APPLICATION_REGISTRATION_FAILED 	Fails to start scan as app cannot be registered.
                       int 	SCAN_FAILED_FEATURE_UNSUPPORTED 	Fails to start power optimized scan as this feature is not supported.
                       int 	SCAN_FAILED_INTERNAL_ERROR 	Fails to start scan due an internal error */
            switch (errorCode) {
                case SCAN_FAILED_ALREADY_STARTED:
                    Log.e(TAG, "Failed to start scan: scan already started.");
                    break;
                case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    Log.e(TAG, "Failed to start scan: app cannot be registered.");
                    break;
                case SCAN_FAILED_FEATURE_UNSUPPORTED:
                    Log.e(TAG, "Failed to start scan: feature unsupported.");
                    break;
                case SCAN_FAILED_INTERNAL_ERROR:
                    Log.e(TAG, "Failed to start scan: internal error.");
                    break;
                default:
                    Log.e(TAG, "Failed to start scan: unknown error.");
                    break;
            }
            ;
                    /* and notify our app of the failure */
            /*
            LocalBroadcastManager.getInstance(ctx).
                    sendBroadcast(new Intent(RT2Const.INTENT_scanFailed).
                            putExtra("errorCode", errorCode));
                            */
        }
    };

    public BluetoothDevice deviceForAddress(String address) {
        if (address == null) {
            Log.e(TAG,"deviceForAddress: address is null");
            return null;
        }
        BluetoothDevice device = deviceHashMap.get(address);
        if (device == null) {
            Log.e(TAG,"deviceForAddress: failed to find device '"+address+"' in table");
            Log.e(TAG,"deviceForAddress: table has " + deviceHashMap.size() + " entries.");
            for (String k : deviceHashMap.keySet()) {
                Log.e(TAG,"deviceForAddress: key="+k);
            }
        }
        return device;
    }

}
