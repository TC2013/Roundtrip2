package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;


import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

public class PumpAlarmPumpEvent extends TimeStampedRecord {
    private int rawtype = 0;
    public PumpAlarmPumpEvent() {
    }
    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        if (!simpleParse(9,data,4)) {
            return false;
        }
        rawtype = asUINT8(data[1]);
        addValue("rawtype",rawtype);
        return true;
    }

}
