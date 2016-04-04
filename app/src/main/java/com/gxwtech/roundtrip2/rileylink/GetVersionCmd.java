package com.gxwtech.roundtrip2.rileylink;

/**
 * Created by geoff on 4/2/16.
 */
public class GetVersionCmd extends CmdBase {
    private byte[] cmd;
    private byte[] mData;
    public GetVersionCmd() {
        super();
        cmd = new byte[1];
        cmd[0] =  RILEYLINK_CMD_GET_VERSION;
    }
    public byte[] getData() {
        return mData;
    }
}
