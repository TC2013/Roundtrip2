package com.gxwtech.roundtrip2.CommunicationService.Objects;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Tim on 07/06/2016.
 * Logs treatments to local DB, currently only Bolues requests
 */
@Table(name = "treatments", id = BaseColumns._ID)
public class Treatment extends Model {

    @Column(name = "type")
    public String   type;                       //Treatment type
    @Column(name = "date_requested")
    public Long  date_requested;                //Date requested
    @Column(name = "date_delivered")
    public Long  date_delivered;                //Date delivered
    @Column(name = "value")
    public Double  value;                       //Treatment amount
    @Column(name = "state")
    public String  state;                       //Current state of this treatment
    @Column(name = "details")
    public String  details;                     //Any details of actioning this treatment
    @Column(name = "delivered")
    public Boolean delivered;                   //Has the treatment been delivered?
    @Column(name = "rejected")
    public Boolean rejected;                    //Has the treatment been rejected and should never be processed?
    @Column(name = "aps_int_id")
    public Long  aps_int_id;                    //Integration ID provided by APS app
    @Column(name = "aps_update")
    public Boolean aps_update;                  //Do we need to update APS app of a change?
    @Column(name = "auth_code")
    public String  auth_code;                   //UID of this Treatments Integration requested provided by APS app, used to authenticate with APS app when updating this treatment

    public Treatment() {
        type            = "";
        date_requested  = 0L;
        date_delivered  = 0L;
        value           = 0D;
        state           = "";
        details         = "";
        delivered       = false;
        rejected        = false;
        aps_int_id      = null;
        aps_update      = false;
        auth_code       = null;
    }

    public static List<Treatment> getLatestTreatments(int limit) {
        return new Select()
                .from(Treatment.class)
                .orderBy("date_requested desc")
                .limit(limit)
                .execute();
    }

    public static List<Treatment> getToBeActioned() {
        return new Select()
                .from(Treatment.class)
                .where("rejected = 0") //false
                .where("delivered = 0") //false
                .orderBy("date_requested desc")
                .execute();
    }

    public static List<Treatment> getToUpdateAPSApp() {
        return new Select()
                .from(Treatment.class)
                .where("aps_update = 1") //true
                .orderBy("date_requested desc")
                .execute();
    }

    public static List<Treatment> getTreatmentsDated(Long dateFrom, Long dateTo) {
        return new Select()
                .from(Treatment.class)
                .where("date_requested >= ? and date_requested <= ?", dateFrom, dateTo)
                .orderBy("date_requested desc")
                .execute();
    }

    public static JSONObject getHAPPJSON(Treatment treatment){
        JSONObject reply = new JSONObject();

        try {
            reply.put("APS_INT_ID", treatment.aps_int_id);
            reply.put("STATE", treatment.state);
            reply.put("DETAILS", treatment.details);
            reply.put("INTEGRATION_CODE", treatment.auth_code);
            reply.put("ID", treatment.getId());
        } catch (JSONException e){
            e.printStackTrace();
        }

        return reply;
    }
}
