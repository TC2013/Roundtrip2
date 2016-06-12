package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

public class CalBgForPhPumpEvent extends TimeStampedRecord {
    private int amount = 0;
    public CalBgForPhPumpEvent() {
    }

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        if (!simpleParse(7,data,2)) {
            return false;
        }
        amount = ((asUINT8(data[6]) & 0x80) << 1) + asUINT8(data[1]);
        addValue("amount",amount);
        return true;
    }
}
