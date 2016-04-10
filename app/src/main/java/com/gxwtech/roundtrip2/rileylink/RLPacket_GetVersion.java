package com.gxwtech.roundtrip2.rileylink;

/**
 * Created by geoff on 4/9/16.
 */
public class RLPacket_GetVersion extends RileyLinkPacket {
    public RLPacket_GetVersion() {
        super(RILEYLINK_CMD_GET_VERSION);
    }
}
