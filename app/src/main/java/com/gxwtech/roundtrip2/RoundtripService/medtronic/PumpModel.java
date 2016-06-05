package com.gxwtech.roundtrip2.RoundtripService.medtronic;
// cribbed from:
//package com.nightscout.core.drivers.Medtronic;

/**
 * Created by geoff on 5/13/15.
 */

public enum PumpModel {
    UNSET,
    MM508,
    MM515,
    MM522,
    MM523;
    public static boolean isLargerFormat(PumpModel model) {
        if (model == MM523) {
            return true;
        }
        return false;
    }
}