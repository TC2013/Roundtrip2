package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;


import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

/**
 * Created by geoff on 6/5/16.
 */
public class AlarmSensorPumpEvent extends TimeStampedRecord {
    public AlarmSensorPumpEvent() {}

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        return simpleParse(8,data,2);
    }

    @Override
    public String getShortTypeName() {
        return "Alarm Sensor";
    }
}
