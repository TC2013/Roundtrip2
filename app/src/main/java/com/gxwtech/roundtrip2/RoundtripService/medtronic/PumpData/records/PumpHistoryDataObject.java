package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;

/**
 * Created by geoff on 6/11/16.
 * Base class for viewable items in the pump history.
 * Most of these are simple strings or values, but can be complex objects.
 */

public class PumpHistoryDataObject {
    public PumpHistoryDataObject() {}
    private Object object;
    public PumpHistoryDataObject(Object object) {
        this.object = object;
    }
    public Object getValue() {
        return object;
    }
}
