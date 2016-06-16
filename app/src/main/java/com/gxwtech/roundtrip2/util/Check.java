package com.gxwtech.roundtrip2.util;

import com.gxwtech.roundtrip2.CommunicationService.Objects.Basal;
import com.gxwtech.roundtrip2.CommunicationService.Objects.ObjectToSync;
import com.gxwtech.roundtrip2.CommunicationService.Objects.Treatment;

import java.util.Date;

/**
 * Created by Tim on 16/06/2016.
 * Class that provides validation and safety checks
 */
public class Check {
    final static int MaxTreatmentAgeInMins = 10;

    public static String NewObjectToSync(ObjectToSync objectToSync){
        // New incoming object to sync, checks we are happy to accept
        String result = "";

        result  +=  PumpSupported(objectToSync);

        // TODO: 21/02/2016 perform other checks here, return empty string for OK or text detailing the issue

        return result;
    }

    public static boolean IsBasalSafeToAction(Basal basal){
        //Are we happy to Action this requested new or cancel Basal request

        Boolean isPumpOnline    =   true;                                                           // TODO: 16/01/2016 this should be a function to check if the pump is online
        Boolean isPumpBusy      =   false;                                                          // TODO: 16/01/2016 this should be a function to check if the pump is busy, for example if delivering a treatment
        if (!isPumpOnline || isPumpBusy) {                                                          //Cannot use pump right now
            basal.state         =   "delayed";
            basal.details       =   "Pump online: " + isPumpOnline + " | Pump busy: " + isPumpBusy;
            basal.been_set      =   false;
            basal.aps_update   =   true;
            basal.save();

            return false;
        }

        switch (basal.action) {

            case "new":

                if (RequestIsTooOld(basal.start_time.getTime())){
                    basal.state         = "error";
                    basal.details       = "NEW Basal Request is older than 10mins, too old to be set";
                    basal.been_set      = false;
                    basal.rejected      = true;
                    basal.aps_update   = true;
                    basal.save();

                    return false;
                }

                // TODO: 16/06/2016 add additional checks for new basal requests

                break;

            case "cancel":

                if (!IsThisBasalLastActioned(basal)) {
                    basal.state         =   "error";
                    basal.details       =   "Current Running Temp Basal does not match this Cancel request, Temp Basal has not been canceled";
                    basal.been_set      =   false;
                    basal.aps_update   =   true;
                    basal.rejected      =   true;
                    basal.save();

                    return false;
                }

                // TODO: 16/06/2016 add additional checks for cancel basal requests

                break;
        }

        return true;                                                                                //All good to action
    }

    public static boolean IsBolusSafeToAction(Treatment bolus){
        //Are we happy to Action this requested Bolus request

        if (RequestIsTooOld(bolus.date_requested)) {
            bolus.state         =   "error";
            bolus.details       =   "Treatment is older than 10mins, too old to be automated";
            bolus.delivered     =   false;
            bolus.rejected      =   true;
            bolus.aps_update   =   true;
            bolus.save();

            return false;
        }

        Boolean isPumpOnline    =   true;                                                           // TODO: 16/01/2016 this should be a function to check if the pump is online
        Boolean isPumpBusy      =   false;                                                          // TODO: 16/01/2016 this should be a function to check if the pump is busy, for example if delivering a treatment
        if (!isPumpOnline || isPumpBusy) {                                                          //Cannot use pump right now
            bolus.state         =   "delayed";
            bolus.details       =   "Pump online: " + isPumpOnline + " | Pump busy: " + isPumpBusy;
            bolus.delivered     =   false;
            bolus.aps_update   =   true;
            bolus.save();

            return false;
        }

        if (bolus.value == 1.1){
            // TODO: 14/02/2016 This must be removed in production
            bolus.state         =   "error";
            bolus.details       =   "test error as value = 1.1";
            bolus.delivered     =   false;
            bolus.rejected      =   true;
            bolus.aps_update   =   true;
            bolus.save();

            return false;
        }

        // TODO: 16/06/2016 add additional Bolus safety checks here

        return true;                                                                                //All good to action
    }

    private static String PumpSupported(ObjectToSync objectToSync){
        //Do we support the pump requested?
        switch (objectToSync.value4){
            case "medtronic_absolute":
            case "medtronic_percent":
                return "";
            default:
                return "Pump requested not supported, treatment rejected.";
        }
    }

    private static boolean RequestIsTooOld(Long date){
        //Is this request too old to action?
        Long ageInMins = (new Date().getTime() - date) /1000/60;
        if (ageInMins > MaxTreatmentAgeInMins){
            return true;
        } else {
            return false;
        }
    }

    private static boolean IsThisBasalLastActioned(Basal basal){
        //Is this Basal the most recent one set to the pump?
        Basal lastActive        =   Basal.lastActive();                                             //Basal that is active or last active

        if (lastActive == null){
            return false;                                                                           //We are not aware of any Basal set by this App
        }

        if (!lastActive.aps_int_id.equals(basal.aps_int_id)) {
            return false;                                                                           //Basal to cancel does not match the current active basal
        }

        return true;
    }

}

