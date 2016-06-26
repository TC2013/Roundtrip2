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
    // commandID is a string that the client can set on the message.
    // The service does not use this value, but passes it back with the result
    // so that the client can identify it.
    public ServiceCommand(String commandName, String commandID) {
        params = new Bundle();
        params.putString("command",commandName);
        params.putString("commandID",commandID);
    }
    public Bundle getParameters() {
        return params;
    }
    public void setParameters(Bundle b) {
        params = b;
    }
    public String getCommandID() {
        return params.getString("commandID");
    }
    public String getCommandName() {
        return params.getString("command");
    }
}
