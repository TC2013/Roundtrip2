package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;

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

}
