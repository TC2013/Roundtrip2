package com.gxwtech.roundtrip2.rileylink;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.gxwtech.roundtrip2.bluetooth.GattAttributes_RileyLinkRFSpy;
import com.gxwtech.roundtrip2.util.ByteUtil;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by geoff on 4/3/16.
 */
public class RLCharacteristic {
    private static final String TAG = "RLCharacteristic";
    protected BluetoothGattCharacteristic chara;
    protected BluetoothGatt mGatt;
    protected Context mContext;
    protected byte[] data;
    protected static Semaphore deviceAccess = new Semaphore(1,true);
    protected static Semaphore waitForCallback = new Semaphore(0,true);

    public RLCharacteristic(Context ctx, BluetoothGatt gatt, BluetoothGattCharacteristic chara) {
        this.chara = chara;
        this.mContext = ctx;
        this.mGatt = gatt;
    }

    public String getName() {
        return GattAttributes_RileyLinkRFSpy.lookup(chara.getUuid().toString(),"unknown characteristic");
    }

    public boolean reliableWriteBlocking(byte[] value) {
        data = value;
        mGatt.beginReliableWrite();
        chara.setValue(value);
        mGatt.writeCharacteristic(chara);
        return false;
        // NYI

    }
    public byte[] readMyChara() {
        byte[] rval = chara.getValue();
        Log.i(TAG,"(Read "+getName()+")("+ByteUtil.shortHexString(rval)+")");
        return rval;
    }
    /*
     When reading or writing a characteristic, first grab the device access semaphore
     from the service thread.

     Second, start the asynch process (read or write), and wait on the dataAvailable semaphore,
     which shouldn't be available until released by the callback.  When it is released, do your
     read or write, then re-lock the data available semaphore so that it can only be released
     by the device callback.
     */

    // doReadBlocking is called from the main Service thread (ONLY!!)
    public byte[] doReadBlocking() {
        //Log.w(TAG,"doReadBlocking: device access: "+deviceAccess.toString());
        //Log.w(TAG,"doReadBlocking: waitForCallback: "+waitForCallback.toString());
        boolean acquired;
        try {
            acquired = deviceAccess.tryAcquire(DEFAULT_SEMAPHORE_WAITTIME_MS, TimeUnit.MILLISECONDS);
            if (acquired) {
                // start the asynch read process.  If it fails, release sema.
                int retries = 50;
                boolean success = false;
                while (retries-- > 0) {
                    if (mGatt.readCharacteristic(chara)) {
                        success = true;
                        break;
                    }
                    SystemClock.sleep(50/*ms*/);

                }
                if (!success) {
                    Log.e(TAG, "doReadBlocking: readChara failed:" + getName());
                    deviceAccess.release();
                    return null;
                }
                // Read request dispatched.
                // now wait for data
                try {
                    // block here until data is available. [DATA-AVAIL]
                    // This also re-locks the waitForCallback semaphore.
                    boolean gotCallback;
                    gotCallback = waitForCallback.tryAcquire(DEFAULT_SEMAPHORE_WAITTIME_MS, TimeUnit.MILLISECONDS);
                    if (gotCallback) {
                        // Data has become available, go get it.
                        data = readMyChara();
                        //Log.d(TAG,"doReadBlocking: success");
                    } else {
                        Log.e(TAG,"doReadBlocking: timeout waiting for callback:"+getName());
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "doReadBlocking: interrupted waiting for data:" + getName());
                }
                deviceAccess.release();
                return data;
            } else {
                Log.e(TAG,"doReadBlocking: timeout waiting for device access:"+getName());
            }
        } catch (InterruptedException e) {
            Log.e(TAG,"doReadBlocking: timeout waiting for deviceAccess semaphore:"+getName());
        }
        return null;
    }

    public void didRead(int status) {
        // Callback is informing us that the value-read has been completed, we can use the data.
        // This is being run on callback thread.
        // get value
        Log.d(TAG,String.format("didRead(status=%d)",status));
        waitForCallback.release();  // UNBLOCKS service thread at [DATA-AVAIL]
    }

    private void writeMyChara(byte[] value) {
        Log.i(TAG,"(Write "+getName()+")("+ ByteUtil.shortHexString(value)+")");
        chara.setValue(value);
    }

    public static final int DEFAULT_SEMAPHORE_WAITTIME_MS = 5000;


    public void doWriteBlocking(byte[] value) {
        //Log.w(TAG,"doWriteBlocking: device access: "+deviceAccess.toString());
        //Log.w(TAG,"doWriteBlocking: waitForCallback: "+waitForCallback.toString());
        try {
            boolean acquired = deviceAccess.tryAcquire(DEFAULT_SEMAPHORE_WAITTIME_MS, TimeUnit.MILLISECONDS);
            if (acquired) {
                //chara.setValue(value);
                writeMyChara(value);
                // start the asynch write process.  If write fails, release sema.
                if (!mGatt.writeCharacteristic(chara)) {
                    Log.e(TAG, "doWriteBlocking: writeCharacteristic failed");
                    deviceAccess.release();
                    return;
                }
                // wait for the callback
                try {
                    // Block until the callback says it completed the write.
                    // this also re-locks the waitForCallback semaphore.
                    boolean gotCallback = waitForCallback.tryAcquire(DEFAULT_SEMAPHORE_WAITTIME_MS, TimeUnit.MILLISECONDS);
                    if (!gotCallback) {
                        Log.e(TAG,"doWriteBlocking: timeout waiting for callback:" +getName());
                    } else {
                        //Log.d(TAG,"doWriteBlocking: write operation complete.");
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "doWriteBlocking: interrupted waiting for callback:" + getName());
                }
                deviceAccess.release();
            } else {
                Log.e(TAG,"doWriteBlocking: timeout waiting for access control semaphore:"+getName());
            }
        } catch (InterruptedException e) {
            Log.e(TAG,"doWriteBlocking: interrupted waiting for access control semaphore:"+getName());
        }
    }

    public void didWrite(int status) {
        // Callback is informing us that the value-write has been completed.
        // This is being run on callback thread.
        Log.d(TAG,String.format("didWrite(status=%d)",status));
        waitForCallback.release();
    }

}
