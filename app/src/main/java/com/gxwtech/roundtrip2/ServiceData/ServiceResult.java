package com.gxwtech.roundtrip2.ServiceData;

import android.os.Bundle;

import java.util.HashMap;

/**
 * Created by geoff on 6/25/16.
 */
public class ServiceResult {
    Bundle map = new Bundle();
    public ServiceResult() { init(); }
    public void init() {
        map.putString("ServiceResultType",this.getClass().getSimpleName());
        setResultError(0,"Uninitialized ServiceResult");
    }
    public void setResultOK() {
        map.putString("result","OK");
    }
    public void setResultError(int errorCode) {
        setResultError(errorCode,getErrorDescription(errorCode));
    }
    public void setResultError(int errorCode, String errorDescription) {
        map.putString("result","error");
        map.putInt("errorCode",errorCode);
        map.putString("errorDescription",errorDescription);
    }
    protected Bundle getMap() {
        return map;
    }
    protected void setMap(Bundle map) {
        this.map = map;
    }

    public static final int ERROR_MALFORMED_PUMP_RESPONSE = 1;
    public static final int ERROR_NULL_PUMP_RESPONSE = 2;
    public static final int ERROR_INVALID_PUMP_RESPONSE = 3;

    public static final String getErrorDescription(int errorCode) {
        switch(errorCode) {
            case ERROR_MALFORMED_PUMP_RESPONSE: return "Malformed Pump Response";
            case ERROR_NULL_PUMP_RESPONSE: return "Null pump response";
            case ERROR_INVALID_PUMP_RESPONSE: return "Invalid pump response";
            default: return "Unknown error code (" + errorCode + ")";
        }
    }
    public Bundle getResponseBundle() {
        // allows for any final processing on the bundle that we need to do
        return getMap();
    }
}
