package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;

/**
 * Created by geoff on 7/16/16.
 */
public class UnknownInsulinCaptureEvent extends TimeStampedRecord {
    public UnknownInsulinCaptureEvent() {}

    /*
     Darrell Wright:
     it is a manual entry of a bolus that the pump didn't deliver, so opcode, timestamp and at least a number to represent the units of insulin
    */

    @Override
    public String getShortTypeName() {
        return "UnknownInsulin";
    }
}
