package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;


import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

public class ChangeBolusWizardSetupPumpEvent extends TimeStampedRecord {

    public ChangeBolusWizardSetupPumpEvent() {

    }

    @Override
    public String getShortTypeName() {
        return "Ch Bolus Wizard Setup";
    }

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        return simpleParse(144,data,2);
    }
}
