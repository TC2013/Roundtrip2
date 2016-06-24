package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

public class ResultDailyTotalPumpEvent extends TimeStampedRecord {
    private static final String TAG = "ResultDailyTotalPumpEvent";
    public ResultDailyTotalPumpEvent() {
    }

    @Override
    public int getLength() { return PumpModel.isLargerFormat(model) ? 10 : 7; }

    @Override
    public String getShortTypeName() {
        return "Result Daily Total";
    }

}
