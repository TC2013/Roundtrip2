package com.gxwtech.roundtrip2.rileylink;

/**
 * Created by geoff on 4/2/16.
 */
public class CmdBase {
    protected byte[] response;
    protected static final int RILEYLINK_CMD_GET_STATE = 1;
    protected static final int RILEYLINK_CMD_GET_VERSION = 2;
    protected static final int RILEYLINK_CMD_GET_PACKET = 3;
    protected static final int RILEYLINK_CMD_SEND_PACKET = 4;
    protected static final int RILEYLINK_CMD_SEND_AND_LISTEN = 5;
    protected static final int RILEYLINK_CMD_UPDATE_REGISTER = 6;
    protected static final int RILEYLINK_CMD_RESET = 7;

    public CmdBase() {
    }
    public byte[] getData() {
        return null;
    }
    public byte[] getResponse() {
        return response;
    }
}
