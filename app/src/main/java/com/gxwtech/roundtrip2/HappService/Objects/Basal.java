package com.gxwtech.roundtrip2.HappService.Objects;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.Date;
import java.util.List;

/**
 * Created by Tim on 07/06/2016.
 */
@Table(name = "basal", id = BaseColumns._ID)
public class Basal extends Model {

    @Column(name = "rate")
    public Double   rate;                   //Temp Basal Rate for (U/hr) mode
    @Column(name = "ratePercent")
    public Integer  ratePercent;            //Temp Basal Rate for "percent" of normal basal
    @Column(name = "duration")
    public Integer  duration;               //Duration of Temp
    @Column(name = "start_time")
    public Date start_time;                 //When the Temp Basal started

    @Column(name = "action")
    public String  action;                      //new / cancel
    @Column(name = "state")
    public String  state;                       //Current state of this basal
    @Column(name = "details")
    public String  details;                     //Any details of actioning this basal
    @Column(name = "been_set")
    public Boolean been_set;                         //Has the basal been set?
    @Column(name = "rejected")
    public Boolean rejected;                    //Has the basal been rejected and should never be processed?
    @Column(name = "happ_int_id")
    public Long  happ_int_id;                   //Integration ID provided by HAPP
    @Column(name = "happ_update")
    public Boolean happ_update;                 //Do we need to update HAPP of a change?
    @Column(name = "auth_code")
    public String  auth_code;                   //UID of this Integration requested provided by HAPP, used to authenticate with HAPP when updating

    public Basal() {
        rate            = 0D;
        ratePercent     = 0;
        duration        = 0;
        start_time      = null;

        action          = "";
        state           = "";
        details         = "";
        been_set        = false;
        rejected        = false;
        happ_int_id     = null;
        happ_update     = false;
        auth_code       = null;
    }

    public static List<Basal> getLatest(int limit) {
        return new Select()
                .from(Basal.class)
                .orderBy("start_time desc")
                .limit(limit)
                .execute();
    }

    public static Basal lastActive() {
        Basal last = new Select()
                .from(Basal.class)
                .where("been_set = 1") //true
                .orderBy("start_time desc")
                .executeSingle();

        return last;
    }

    public static Basal lastRequested() {
        Basal last = new Select()
                .from(Basal.class)
                .where("been_set = 0") //false
                .where("rejected = 0") //false
                .orderBy("start_time desc")
                .executeSingle();

        return last;
    }

    public static List<Basal> getToUpdateHAPP() {
        return new Select()
                .from(Basal.class)
                .where("happ_update = 1") //true
                .orderBy("start_time desc")
                .execute();
    }
}
