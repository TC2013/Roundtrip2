package com.gxwtech.roundtrip2.ServiceData;

import android.os.Bundle;

import java.util.HashMap;

/**
 * Created by geoff on 6/25/16.
 */
public class ServiceResult {
    Bundle map;
    public ServiceResult() {}
    public void init() {
        map.putString("ServiceResultType",this.getClass().getSimpleName());
        map.putString("result","error");
        map.putInt("errorCode",0);
        map.putString("errorDescription","Uninitialized ClientResult");
    }
    public Bundle getMap() {
        return map;
    }
    public void setMap(Bundle map) {
        this.map = map;
    }
}
