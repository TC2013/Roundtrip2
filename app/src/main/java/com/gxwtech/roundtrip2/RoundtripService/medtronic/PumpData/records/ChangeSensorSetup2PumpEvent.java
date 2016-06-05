package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

/**
 * Created by geoff on 6/5/16.
 */
public class ChangeSensorSetup2PumpEvent extends TimeStampedRecord {
    public ChangeSensorSetup2PumpEvent() {}

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        return simpleParse(37,data,2);
    }
}
