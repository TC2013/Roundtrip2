package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;


import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

public class EnableDisableRemotePumpEvent extends TimeStampedRecord {
    public EnableDisableRemotePumpEvent() {
    }
    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        return simpleParse(21,data,2);
    }
}
