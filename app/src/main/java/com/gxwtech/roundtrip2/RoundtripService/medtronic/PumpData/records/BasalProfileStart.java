package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

public class BasalProfileStart extends TimeStampedRecord {
    private static final String TAG = "BasalProfileStart";
    private int offset = 0;
    private double rate = 0.0;
    private int profileIndex = 0;

    public BasalProfileStart() {
        length = 10;
    }

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        if (!simpleParse(10,data,2)) {
            return false;
        }

        profileIndex = asUINT8(data[1]);
        offset = asUINT8(data[7]) * 30 * 1000 * 60;
        rate = (double)(asUINT8(data[8])) / 40.0;
        addValue("offset",offset);
        addValue("rate",rate);
        addValue("profileIndex",profileIndex);
        return true;
    }
}
