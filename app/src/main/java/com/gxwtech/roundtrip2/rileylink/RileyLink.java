package com.gxwtech.roundtrip2.rileylink;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.gxwtech.roundtrip2.Minimed;
import com.gxwtech.roundtrip2.bluetooth.GattAttributes_RileyLinkRFSpy;
import com.gxwtech.roundtrip2.util.ByteUtil;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by geoff on 3/21/16.
 */
public class RileyLink {
    private final static String TAG = "RileyLink";
    private Context ctx;
    private BluetoothDevice mDevice;
    private BluetoothGatt mGatt;
    private final BluetoothGattCallback mGattCallback;
    protected RileyLinkPacket currentCmd;
    protected Minimed mPump;
    protected static Semaphore waitForData = new Semaphore(0,true);


    //private BluetoothGattCharacteristic responseCountCharacteristic;
    //private BluetoothGattCharacteristic dataCharacteristic;
    //private BluetoothGattCharacteristic customNameCharacteristic;
    //private BluetoothGattCharacteristic timerTickCharacteristic;
    private RLCharacteristic responseCountCharacteristic;
    private RLCharacteristic dataCharacteristic;
    private RLCharacteristic customNameCharacteristic;
    private RLCharacteristic timerTickCharacteristic;
    private HashMap<BluetoothGattCharacteristic, RLCharacteristic> charaMap = new HashMap<>(4);

    public RileyLink(Context ctx) {
        this.ctx = ctx;
        mPump = new Minimed();
        mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                Log.d(TAG, "RileyLink:onConnectionStateChange(" + status + "," + newState + ")");
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        Log.i(TAG, "Connected to GATT Server");
                        mGatt.discoverServices();
                        break;
                    case BluetoothProfile.STATE_CONNECTING:
                        Log.i(TAG, "Connecting to GATT Server");
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        Log.i(TAG, "Disconnected from GATT Server");
                        break;
                    case BluetoothProfile.STATE_DISCONNECTING:
                        Log.i(TAG, "Disconnecting from GATT Server");
                        break;
                    default:
                        Log.i(TAG, "Unknown connection state: " + newState);
                        break;
                }
            }
            /* Decompiled from BluetoothGatt: status values
            public static final int GATT_CONNECTION_CONGESTED = 143;
            public static final int GATT_FAILURE = 257;
            public static final int GATT_INSUFFICIENT_AUTHENTICATION = 5;
            public static final int GATT_INSUFFICIENT_ENCRYPTION = 15;
            public static final int GATT_INVALID_ATTRIBUTE_LENGTH = 13;
            public static final int GATT_INVALID_OFFSET = 7;
            public static final int GATT_READ_NOT_PERMITTED = 2;
            public static final int GATT_REQUEST_NOT_SUPPORTED = 6;
            public static final int GATT_SUCCESS = 0;
            public static final int GATT_WRITE_NOT_PERMITTED = 3;
            */


            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                Log.d(TAG, "onServicesDiscovered(" + status + ")");
                super.onServicesDiscovered(gatt, status);
                setCharacteristicsFromServices();
                //dumpServices();
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                RLCharacteristic rlChara = charaMap.get(characteristic);
                if (rlChara != null) {
                    rlChara.didRead(status);
                } else {
                    Log.e(TAG, "onCharacteristicRead: RLChara not found in map");
                }
                Log.d(TAG, "onCharacteristicRead(" + characteristic.toString() + "," + status + ")");
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                RLCharacteristic rlChara = charaMap.get(characteristic);
                if (rlChara != null) {
                    rlChara.didWrite(status);
                } else {
                    Log.e(TAG, "onCharacteristicWrite: RLChara not found in map");
                }
                Log.d(TAG, "onCharacteristicWrite(" + characteristic.toString() + "," + status + ")");
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                //Log.d(TAG,"onCharacteristicChanged("+characteristic.toString()+")");
                RLCharacteristic rlChara = charaMap.get(characteristic);
                if (rlChara != null) {
                    if (rlChara.equals(responseCountCharacteristic)) {
                        didUpdateData();
                    } else {
                        Log.e(TAG,"onCharacteristicChanged: can't update ?!?!?!");
                    }
                    Log.e(TAG, "onCharacteristicChanged: " + rlChara.getName());
                } else {
                    Log.e(TAG, "onCharacteristicChanged: RLChara not found in map");
                }
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
                Log.d(TAG, "onDescriptorRead(" + descriptor.toString() + "," + status + ")");
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                Log.d(TAG, "onDescriptorWrite(" + descriptor.toString() + "," + status + ")");
            }

            @Override
            public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                super.onReliableWriteCompleted(gatt, status);
                Log.d(TAG, "onReliableWriteCompleted(" + status + ")");
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                Log.d(TAG, "onReadRemoteRssi(" + rssi + "," + status + ")");
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
                Log.d(TAG, "onMtuChanged(" + mtu + "," + status + ")");
            }
        };
    }

    public void init(BluetoothDevice device) {
        mDevice = device;
        if (device != null) {
            Log.d(TAG, "init: connecting to GATT for device " + device.getAddress());
            mGatt = device.connectGatt(ctx, true, mGattCallback);
        } else {
            Log.e(TAG, "init: cannot connect (null BLE device address)");
        }
    }

    public void setCharacteristicsFromServices() {
        if (mGatt == null) {
            Log.e(TAG, "mGatt is null");
            return;
        }
        List<BluetoothGattService> serviceList = mGatt.getServices();
        if (serviceList.isEmpty()) {
            Log.e(TAG, "serviceList is empty");
        }
        for (BluetoothGattService service : serviceList) {
            UUID serviceUUID = service.getUuid();
            Log.i(TAG, "Service uuid: " + serviceUUID.toString());
            Log.i(TAG, "Service Characteristics:");
            List<BluetoothGattCharacteristic> charas = service.getCharacteristics();
            for (BluetoothGattCharacteristic chara : charas) {
                if (chara.getUuid().toString().equals(GattAttributes_RileyLinkRFSpy.CHARA_RADIO_DATA)) {
                    dataCharacteristic = new RLCharacteristic(ctx, mGatt, chara);
                    charaMap.put(chara, dataCharacteristic);
                    //dataCharacteristic = chara;
                    Log.w(TAG, "Found data characteristic");
                } else if (chara.getUuid().toString().equals(GattAttributes_RileyLinkRFSpy.CHARA_RADIO_CUSTOM_NAME)) {
                    customNameCharacteristic = new RLCharacteristic(ctx, mGatt, chara);
                    charaMap.put(chara, customNameCharacteristic);
                    //customNameCharacteristic = chara;
                    Log.w(TAG, "Found customName characteristic");
                } else if (chara.getUuid().toString().equals(GattAttributes_RileyLinkRFSpy.CHARA_RADIO_TIMER_TICK)) {
                    timerTickCharacteristic = new RLCharacteristic(ctx, mGatt, chara);
                    charaMap.put(chara, timerTickCharacteristic);
                    //timerTickCharacteristic = chara;
                    Log.w(TAG, "Found timerTick characteristic");
                } else if (chara.getUuid().toString().equals(GattAttributes_RileyLinkRFSpy.CHARA_RADIO_RESPONSE_COUNT)) {
                    responseCountCharacteristic = new RLCharacteristic(ctx, mGatt, chara);
                    charaMap.put(chara, responseCountCharacteristic);
                    //responseCountCharacteristic = chara;
                    Log.w(TAG, "Found responseCount characteristic");
                    // enable local notification
                    mGatt.setCharacteristicNotification(chara, true);
                    // enable remote notification
                    BluetoothGattDescriptor desc = chara.getDescriptors().get(0);
                    desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    if (mGatt.writeDescriptor(desc)) {
                        Log.d(TAG, "Succesfully wrote descriptor");
                    } else {
                        Log.d(TAG, "Unable to write descriptor");
                    }
                }
                Log.i(TAG, "--Characteristic uuid: " + chara.getUuid().toString());
                Log.i(TAG, String.format("--properties: %#x", chara.getProperties()));
                Log.i(TAG, "--Descriptors:");
                for (BluetoothGattDescriptor desc : chara.getDescriptors()) {
                    Log.i(TAG, "----UUID: " + desc.getUuid().toString());
                    Log.i(TAG, String.format("----Perms: %#x", desc.getPermissions()));
                }
            }
            if (charaMap.size() != 4) {
                Log.e(TAG, "setCharacteristicsFromServices: failed to find all characteristics for RileyLink");
            }
        }
    }

    public void dumpServices() {
        if (mGatt == null) {
            Log.e(TAG, "mGatt is null");
            return;
        }
        List<BluetoothGattService> serviceList = mGatt.getServices();
        if (serviceList.isEmpty()) {
            Log.e(TAG, "serviceList is empty");
        }
        for (BluetoothGattService service : serviceList) {
            UUID serviceUUID = service.getUuid();
            Log.i(TAG, "Service uuid: " + serviceUUID.toString());
            Log.i(TAG, "Service Characteristics:");
            List<BluetoothGattCharacteristic> charas = service.getCharacteristics();
            for (BluetoothGattCharacteristic chara : charas) {
                Log.i(TAG, "--Characteristic uuid: " + chara.getUuid().toString());
                Log.i(TAG, String.format("--properties: %#x", chara.getProperties()));
                Log.i(TAG, "--Descriptors:");
                for (BluetoothGattDescriptor desc : chara.getDescriptors()) {
                    Log.i(TAG, "----UUID: " + desc.getUuid().toString());
                    Log.i(TAG, String.format("----Perms: %#x", desc.getPermissions()));
                }
            }
        }
    }

    public void writeTo(byte[] data, RLCharacteristic chara) {
        /* first byte is length of rest of message */
        byte dataToWrite[] = new byte[1];
        dataToWrite[0] = (byte) (data.length);
        dataToWrite = ByteUtil.concat(dataToWrite, data);
        chara.doWriteBlocking(dataToWrite);
    }

    public byte[] readFrom(RLCharacteristic chara) {
        return chara.doReadBlocking();
    }

    public byte[] readResponseCount() {
        return readFrom(responseCountCharacteristic);
    }

    public byte[] writeAndRead(byte[] data, RLCharacteristic chara) {
        writeTo(data, chara);
        return readFrom(chara);
    }

    public byte[] writeAndReadData(byte[] data) {
        return writeAndRead(data, dataCharacteristic);
    }

    public void close() {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
    }

    public void testOneRead(RLCharacteristic c) {
        byte[] rval;
        if (c != null) {
            rval = c.doReadBlocking();
            if (rval != null) {
                Log.i(TAG, "test: (len=" + rval.length + ") read " + c.getName() + "=" + ByteUtil.shortHexString(rval));
            } else {
                Log.e(TAG, "test: failed to read " + c.getName());
            }
        }
    }

    public void test() {
        testOneRead(dataCharacteristic);
        testOneRead(timerTickCharacteristic);
        testOneRead(customNameCharacteristic);
        testOneRead(responseCountCharacteristic);
        byte[] rval = writeAndRead(new byte[]{RileyLinkPacket.RILEYLINK_CMD_GET_VERSION}, dataCharacteristic);
        Log.e(TAG, "GetVersionCommand returned " + ByteUtil.shortHexString(rval));
        //RileyLinkPacket hailMary = new SendPacketCmd(1,200,0,new byte[] {(byte) 0xa7, 0x51, (byte) 0x81, 0x63, 0x5d, 0x00});
        //RileyLinkPacket pressTheButton = new SendPacketCmd(1,0,0,new byte[] {(byte) 0xa7, 0x51, (byte)0x81, 0x63, 0x5b, (byte)0x80, 0x01, 0x04});
    }

    public void writeToData(byte[] bytes) {
        writeTo(bytes, dataCharacteristic);
    }

    public byte[] readFromData(int timeoutMS) {
        byte[] rval = null;
        Log.d(TAG,"readFromData waitForData top:"+waitForData.toString());
        boolean dataAvailable = waitForData.tryAcquire();
        if (dataAvailable) {
            Log.d(TAG,"readFromData: data available... going and getting it");
            rval = dataCharacteristic.doReadBlocking();
        } else {
            Log.w(TAG,"readFromData: data not available... waiting for data");
            try {
                // block here until data is available. [DATA-AVAIL]
                // This also re-locks the waitForCallback semaphore.
                dataAvailable = waitForData.tryAcquire(timeoutMS, TimeUnit.MILLISECONDS);
                if (dataAvailable) {
                    // Data has become available, go get it.
                    Log.d(TAG, "readFromData: data has become available, reading....");
                    rval = dataCharacteristic.doReadBlocking();
                } else {
                    Log.e(TAG, "readFromData: timeout waiting for data:");
                    return null;
                }
            } catch (InterruptedException e) {
                Log.e(TAG,"readFromData: interrupted while waiting for waitForData semaphore");
            }
        }
        return rval;
    }

    // called by onCharacteristicChanged callback
    public void didUpdateData() {
        waitForData.release();
    }

    RileyLinkResponse getVersion() {
        RileyLinkPacket pkt = new RLPacket_GetVersion();
        return new RileyLinkResponse(writeAndReadData(pkt.getBytestream()));
    }

    RileyLinkResponse sendAndListen(byte[] payload) {
        RileyLinkPacket pkt = new RLPacket_SendAndListen(payload);
        return new RileyLinkResponse(writeAndReadData(pkt.getBytestream()));
    }
}


