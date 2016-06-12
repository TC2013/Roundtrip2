package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

/**
 * Created by geoff on 6/5/16.
 */
public class ChangeTempBasalTypePumpEvent extends TimeStampedRecord {
    private boolean isPercent=false; // either absolute or percent
    public ChangeTempBasalTypePumpEvent() {}

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        if (!simpleParse(7,data,2)) {
            return false;
        }
        if (asUINT8(data[1])==1) {
            isPercent = true;
        } else {
            isPercent = false;
        }
        addValue("isPercent",isPercent);
        return true;
    }
}
