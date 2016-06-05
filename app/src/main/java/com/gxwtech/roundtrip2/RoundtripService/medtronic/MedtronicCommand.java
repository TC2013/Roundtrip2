package com.gxwtech.roundtrip2.RoundtripService.medtronic;

import android.util.Log;

import com.gxwtech.roundtrip2.RoundtripService.RileyLinkBLE.RFSpy;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.Messages.MessageBody;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.Messages.MessageType;


/**
 * Created by geoff on 4/27/15.
 */
public class MedtronicCommand {
    private static final String TAG = "MedtronicCommand";
    private static final boolean DEBUG_MEDTRONICCOMMAND = false;
    //protected MedtronicCommandEnum mCode;
    MessageType mtype;
    MessageBody mbody;
    protected MedtronicCommandStatusEnum mStatus;
    protected byte[] mPacket;
    protected byte[] mParams; // may be null!

    // button is zero, unless command 93 (SET_POWER_CONTROL) in which case, 85.
    // No, we don't know why :(
    protected byte mButton = 0;
    byte mNRetries = 2;
    byte mBytesPerRecord = 64;
    byte mMaxRecords = 1;

    protected int mSleepForPumpResponse = 100;
    protected int mSleepForPumpRetry = 500; //millis

    public MedtronicCommand() {
        init();
    }

    protected void init() {
        mtype = new MessageType(MessageType.Invalid);
        mPacket = null;
        mParams = null;
        mButton = 0;
    }

    protected void init(byte which) {
        init();
        switch (which) {
            case MessageType.CMD_M_READ_HISTORY:
            default:
        }
    }

    public byte calcRecordsRequired() {
        byte rval;
        int len = mBytesPerRecord * mMaxRecords;
        int i = len / 64;
        int j = len % 64;
        if (j>0) {
            rval = (byte)(i+1);
        } else {
            rval = (byte)i;
        }
        return rval;
    }

    // TODO: figure out how to get notification up to the gui that we're sleeping.
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Log.e(TAG, "Sleep interrupted: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public MedtronicCommandStatusEnum run(RFSpy rfspy, byte[] serialNumber) {
        return mStatus;
    }

}
