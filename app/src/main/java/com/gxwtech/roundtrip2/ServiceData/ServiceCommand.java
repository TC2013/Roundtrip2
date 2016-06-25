package com.gxwtech.roundtrip2.ServiceData;

import android.os.Bundle;

/**
 * Created by geoff on 6/25/16.
 */
public class ServiceCommand {
    protected Bundle params;
    public ServiceCommand() {
        params = new Bundle();
    }
    public ServiceCommand(String commandName) {
        params = new Bundle();
        params.putString("command",commandName);
    }
    public Bundle getParameters() {
        return params;
    }
    public void setParameters(Bundle b) {
        params = b;
    }
}
