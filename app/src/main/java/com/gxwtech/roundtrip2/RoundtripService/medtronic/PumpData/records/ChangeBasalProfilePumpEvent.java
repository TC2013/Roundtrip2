package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;


import android.os.Parcelable;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

public class ChangeBasalProfilePumpEvent extends TimeStampedRecord {
    public ChangeBasalProfilePumpEvent() {
    }

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        return simpleParse(152,data,2);
    }
}
