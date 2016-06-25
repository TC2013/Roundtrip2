package com.gxwtech.roundtrip2.ServiceData;

import android.os.Bundle;

import org.joda.time.LocalDateTime;

/**
 * Created by geoff on 6/25/16.
 */
public class ServiceClientActions {
    public ServiceClientActions() {}

    /*
     *     Set Temp Basal
     *
     *     inputs:
     *     amountUnitsPerHour - temp basal amount, in Units per hour
     *     durationMinutes - temp basal duration, in minutes
     *
     *     result: standard ok/error result
     */

    public ServiceCommand makeSetTempBasalCommand(double amountUnitsPerHour, int durationMinutes) {
        ServiceCommand command = new ServiceCommand("SetTempBasal");
        Bundle b = command.getParameters();
        b.putDouble("amountUnitsPerHour",amountUnitsPerHour);
        b.putInt("durationMinutes",durationMinutes);
        command.setParameters(b);
        return command;
    }

    /*
     *  Read Basal Profile
     *
     *  inputs:
     *  which - "STD", "A", or "B"
     *
     *  result: an ok/error result with a basal profile Bundle.
     *  Get the profile using BasalProfile.initFromServiceResult()
     */

    // 'which' is "STD", "A", or "B"
    public ServiceCommand makeReadBasalProfileCommand(String which) {
        ServiceCommand command = new ServiceCommand("ReadBasalProfile");
        Bundle b = command.getParameters();
        b.putString("which",which);
        command.setParameters(b);
        return command;
    }

    public ServiceCommand makeReadPumpClockCommand() {
        return new ServiceCommand("ReadPumpClock");
    }

    public ServiceCommand makeSendBolusCommand(double amountUnits) {
        ServiceCommand command = new ServiceCommand("SendBolus");
        Bundle b = command.getParameters();
        b.putDouble("amountInUnits",amountUnits);
        command.setParameters(b);
        return command;
    }

    public ServiceCommand makeSetPumpClockCommand(LocalDateTime localDateTime) {
        ServiceCommand command = new ServiceCommand("SetPumpClock");
        Bundle b = command.getParameters();
        b.putString("localDateTime",localDateTime.toString());
        command.setParameters(b);
        return command;
    }

    public ServiceCommand makeReadISFProfileCommand() {
        return new ServiceCommand("ReadISFProfile");
    }

    public ServiceCommand makeReadBolusWizardCarbProfileCommand() {
        return new ServiceCommand("ReadBolusWizardCarbProfile");
    }

    public ServiceCommand makeReadDIASettingCommand() {
        return new ServiceCommand("ReadDIASetting");
    }

    public ServiceCommand makeReadBatteryLevelCommand() {
        return new ServiceCommand("ReadBatteryLevel");
    }

    public ServiceCommand makeReadReservoirLevelCommand() {
        return new ServiceCommand("ReadReservoirLevel");
    }

}
