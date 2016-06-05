package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;


import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

public class TempBasalRatePumpEvent extends TimeStampedRecord {
    private double basalRate = 0.0; // rate in Units/hr
    private boolean mIsPercent = false; // The value is either an absolute number or a percentage

    public TempBasalRatePumpEvent() { }

    public double getBasalRate() { return basalRate; }
    public boolean isPercent() { return mIsPercent; }

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        if (!simpleParse(8,data,2)) {
            return false;
        }
        if ((asUINT8(data[7])>>3)==0) {
            mIsPercent = false;
            basalRate = (double)(asUINT8(data[1])) / 40.0;
        } else {
            mIsPercent = true;
            basalRate = asUINT8(data[1]);
        }
        return true;
    }


}
