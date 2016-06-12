package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;

import android.os.Parcelable;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

/**
 * Created by geoff on 6/5/16.
 */
public class DeleteOtherDeviceIDPumpEvent extends TimeStampedRecord {
    public DeleteOtherDeviceIDPumpEvent(){}

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        return simpleParse(12,data,2);
    }
}
