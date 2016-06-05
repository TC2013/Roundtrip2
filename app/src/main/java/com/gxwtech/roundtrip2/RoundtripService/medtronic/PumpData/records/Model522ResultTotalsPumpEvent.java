package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;


import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

public class Model522ResultTotalsPumpEvent extends TimeStampedRecord {
    public Model522ResultTotalsPumpEvent() {}

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        return simpleParse(44,data,2);
    }
}
