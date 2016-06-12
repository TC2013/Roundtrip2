package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

public class TempBasalDurationPumpEvent extends TimeStampedRecord {
    private int durationMinutes = 0;
    public TempBasalDurationPumpEvent() { }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        if (!simpleParse(7,data,2)) {
            return false;
        }
        durationMinutes = asUINT8(data[1]) * 30;
        return true;
    }

    @Override
    public boolean readFromBundle(Bundle in) {
        durationMinutes = in.getInt("durationMinutes",0);
        return super.readFromBundle(in);
    }

    @Override
    public void writeToBundle(Bundle in) {
        super.writeToBundle(in);
        in.putInt("durationMinutes",durationMinutes);
    }


}
