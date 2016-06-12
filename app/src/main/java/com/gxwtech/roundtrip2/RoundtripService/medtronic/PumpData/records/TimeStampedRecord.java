package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;

import android.os.Bundle;
import android.os.Parcel;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpTimeStamp;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.TimeFormat;

abstract public class TimeStampedRecord extends Record {
    private final static String TAG = "TimeStampedRecord";
    private final static boolean DEBUG_TIMESTAMPEDRECORD = false;

    protected PumpTimeStamp timestamp;

    public TimeStampedRecord() {
        timestamp = new PumpTimeStamp();
    }

    @Override
    public PumpTimeStamp getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        return simpleParse(7,data,2);
    }

    // This is useful if there is no data inside, or we don't care about the data.
    public boolean simpleParse(int myLength, byte[] data, int fiveByteDateOffset) {
        length = myLength;
        if (length > data.length) {
            return false;
        }
        collectTimeStamp(data,fiveByteDateOffset);

        return true;
    }

    protected void collectTimeStamp(byte[] data, int offset) {
        timestamp = new PumpTimeStamp(TimeFormat.parse5ByteDate(data,offset));
    }

    @Override
    public boolean readFromBundle(Bundle in) {
        String timestampString = in.getString("timestamp");
        timestamp = new PumpTimeStamp(timestampString);
        return super.readFromBundle(in);
    }

    @Override
    public void writeToBundle(Bundle in) {
        super.writeToBundle(in);
        in.putString("timestamp",timestamp.toString());

    }
}
