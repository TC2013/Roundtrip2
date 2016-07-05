package com.gxwtech.roundtrip2.ServiceData;

import android.os.Bundle;

/**
 * Created by geoff on 6/25/16.
 */
public class ServiceCommand extends ServiceMessage {
    public ServiceCommand() {
        map = new Bundle();
    }
    // commandID is a string that the client can set on the message.
    // The service does not use this value, but passes it back with the result
    // so that the client can identify it.
    public ServiceCommand(String commandName, String commandID) {
        init();
        map.putString("command",commandName);
        map.putString("commandID",commandID);
    }

    @Override
    public void init() {
        map.putString("ServiceMessageType","ServiceCommand");
    }

    public String getCommandID() {
        return map.getString("commandID");
    }
    public String getCommandName() {
        return map.getString("command");
    }
}
