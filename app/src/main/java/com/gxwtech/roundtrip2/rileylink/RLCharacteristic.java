package com.gxwtech.roundtrip2.rileylink;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import com.gxwtech.roundtrip2.bluetooth.GattAttributes_RileyLinkRFSpy;

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
        try {
            deviceAccess.tryAcquire(500, TimeUnit.MILLISECONDS);
            // start the asynch read process.  If it fails, release sema.
            if (!mGatt.readCharacteristic(chara)) {
                deviceAccess.release();
                Log.e(TAG,"doReadBlocking: readChara failed:"+getName());
                return null;
            }
            // Read request dispatched.
            // now wait for data
            try {
                // block here until data is available. [DATA-AVAIL]
                // This also re-locks the waitForCallback semaphore.
                waitForCallback.tryAcquire(500, TimeUnit.MILLISECONDS);
                // Data has become available, go get it.
                data = chara.getValue();
            } catch (InterruptedException e) {
                Log.e(TAG,"doReadBlocking: timeout waiting for data:"+getName());
            }
            deviceAccess.release();
            return data;
        } catch (InterruptedException e) {
            Log.e(TAG,"doReadBlocking: timeout waiting for deviceAccess semaphore:"+getName());
        }
        return null;
    }

    public void didRead(int status) {
        // Callback is informing us that the value-read has been completed, we can use the data.
        // This is being run on callback thread.
        // get value
        waitForCallback.release();  // UNBLOCKS service thread at [DATA-AVAIL]
    }

    public void doWriteBlocking(byte[] value) {
        try {
            deviceAccess.tryAcquire(500, TimeUnit.MILLISECONDS);
            chara.setValue(value);
            // start the asynch write process.  If write fails, release sema.
            if (!mGatt.writeCharacteristic(chara)) {
                deviceAccess.release();
                Log.e(TAG,"doWriteBlocking: writeCharacteristic failed");
                return;
            }
            // wait for the callback
            try {
                // Block until the callback says it completed the write.
                // this also re-locks the waitForCallback semaphore.
                waitForCallback.tryAcquire(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Log.e(TAG,"doWriteBlocking: timeout waiting for callback:"+getName());
            }
            deviceAccess.release();
        } catch (InterruptedException e) {
            Log.e(TAG,"doWriteBlocking: timeout waiting for access control semaphore");
        }
    }

    public void didWrite(int status) {
        // Callback is informing us that the value-write has been completed.
        // This is being run on callback thread.
        waitForCallback.release();
    }

}
