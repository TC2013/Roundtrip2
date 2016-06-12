package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;


import android.os.Parcelable;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

public class ChangeTimePumpEvent extends TimeStampedRecord {
    public ChangeTimePumpEvent() {

    }

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        return simpleParse(14,data,2);
    }
}
