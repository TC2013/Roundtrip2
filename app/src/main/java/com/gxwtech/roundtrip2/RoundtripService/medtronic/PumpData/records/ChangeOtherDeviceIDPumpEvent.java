package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;


import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

public class ChangeOtherDeviceIDPumpEvent extends TimeStampedRecord {

    public ChangeOtherDeviceIDPumpEvent() {
    }

    @Override
    public String getShortTypeName() {
        return "Ch Other Dev ID";
    }

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        return simpleParse(37,data,2);
    }
}
