package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;


import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

public class Model522ResultTotalsPumpEvent extends TimeStampedRecord {
    public Model522ResultTotalsPumpEvent() {}

    @Override
    public int getLength() { return 44; }

    @Override
    public String getShortTypeName() {
        return "M522 Result Totals";
    }
}
