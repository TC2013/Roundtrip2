package com.gxwtech.roundtrip2.CommunicationService.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;
import java.util.Date;

/**
 * Created by Tim on 07/06/2016.
 */
public class ObjectToSync {

    public String   aps_object_type;            //insulin_treatment / temp_basal
    public String   action;                     //new / update / cancel
    public String   state;                      //to_sync / sent / received / delivered / error
    public String   details;                    //details of this item being synced
    public Long     aps_integration_id;         //ID of the integration record HAPP has
    public Long     remote_id;                  //ID of remote record
    public String   integrationSecretCode;      //Random string to UID this sync request

    public Double   value1;                     //Bolus Amount / Temp Basal Rate
    public String   value2;                     //bolusType (Standard / Square Wave) / Basal %
    public String   value3;                     //Bolus Type (Bolus / Correction) / Temp Basal Duration
    public String   value4;                     //Pump
    public Date     requested;                  //Date requested


    public ObjectToSync (Treatment bolus, Basal basal){

        if (bolus != null){
            //Prepares a Bolus integration to be sent
            aps_object_type         =   "bolus_delivery";
            action                  =   "update";
            state                   =   bolus.state;
            details                 =   bolus.details;
            aps_integration_id      =   bolus.aps_int_id;
            remote_id               =   bolus.getId();
            integrationSecretCode   =   bolus.auth_code;

            value1              =   bolus.value;
            value2              =   "standard";
            value3              =   bolus.type;
            requested           =   new Date(bolus.date_requested);

        } else if (basal != null){
            //Prepares a Temp Basal integration to be sent
            aps_object_type         =   "temp_basal";
            action                  =   basal.action;
            state                   =   basal.state;
            details                 =   basal.details;
            aps_integration_id      =   basal.aps_int_id;
            remote_id               =   basal.getId();
            integrationSecretCode   =   basal.auth_code;

            value1              =   basal.rate;
            value2              =   basal.ratePercent.toString();
            value3              =   basal.duration.toString();
            requested           =   basal.start_time;
        }

    }

    public String asJSONString(){
        Gson gson = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                .serializeNulls()
                .create();
        return gson.toJson(this, ObjectToSync.class);
    }
}
