package com.gxwtech.roundtrip2.rileylink;

import android.util.Log;

import com.gxwtech.roundtrip2.util.ByteUtil;

import java.util.ArrayList;

/**
 * Created by geoff on 4/9/16.
 */
public class RLPacket_Listen extends RileyLinkPacket {
    private static final String TAG = "RLPacket_Listen";
    public RLPacket_Listen() {
        super(new byte[] {RILEYLINK_CMD_GET_PACKET,2,0,0,0,0});
    }
    protected void init() {
        init((byte)2,0);
    }

    public RLPacket_Listen(byte listenChannel, int timeoutMS) {
        super();
        init(listenChannel,timeoutMS);
    }

    protected void init(byte listenChannel, int timeoutMS) {
        initWithData(new byte[] {RILEYLINK_CMD_GET_PACKET, listenChannel,
                (byte)(timeoutMS>>24),(byte)((timeoutMS & 0x00FF0000) >> 16), (byte)((timeoutMS & 0x0000FF00)>>8),(byte)(timeoutMS & 0x000000FF)});
    }

    void setListenChannel(byte listenChannel) {
        data.set(1,listenChannel);
    }
    byte getListenChannel() {
        return byteAt(1);
    }

    void setTimeoutMS(int timeoutMS) {
        data.set(2,(byte)(timeoutMS >> 24));
        data.set(3,(byte)((timeoutMS & 0x00FF0000)>> 16));
        data.set(4,(byte)((timeoutMS & 0x0000FF00)>> 8));
        data.set(5,(byte)((timeoutMS & 0x000000FF)));
    }

    int getTimeoutMS() {
        int rval = 0;
        rval = byteAt(2);
        rval = (rval << 8) | byteAt(3);
        rval = (rval << 8) | byteAt(4);
        rval = (rval << 8) | byteAt(5);
        return rval;
    }

}
