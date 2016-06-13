package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

/**
 * Created by geoff on 6/5/16.
 */
public class DeleteBolusReminderTimePumpEvent extends TimeStampedRecord {
    public DeleteBolusReminderTimePumpEvent() {}

    @Override
    public String getShortTypeName() {
        return "Del Bolus Rmndr Time";
    }

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        return simpleParse(9,data,2);
    }
}
