package com.gxwtech.roundtrip2.rileylink;

import android.util.Log;

import com.gxwtech.roundtrip2.util.ByteUtil;

import java.util.ArrayList;

/**
 * Created by geoff on 4/9/16.
 */
public class RLPacket_SendAndListen extends RileyLinkPacket {
    private static final String TAG = "RLPacket_SendAndListen";
    public RLPacket_SendAndListen() {
        super(new byte[] {RILEYLINK_CMD_SEND_AND_LISTEN,2,0,0,2,0,0,2,0,3});
    }
    protected void init() {
        init((byte)2,(byte)0,(byte)0,(byte)2,500,(byte)3);
    }

    public RLPacket_SendAndListen(byte sendChannel, byte repeatCount, byte packetDelayMS, byte listenChannel, int timeoutMS, byte retryCount) {
        super();
        init(sendChannel,repeatCount,packetDelayMS,listenChannel,timeoutMS,retryCount);
    }

    public RLPacket_SendAndListen(byte[] payload) {
        init();
        setPayload(payload);
    }

    public RLPacket_SendAndListen(byte sendChannel, byte repeatCount, byte packetDelayMS, byte listenChannel, int timeoutMS, byte retryCount, byte[] payload) {
        super();
        init(sendChannel,repeatCount,packetDelayMS,listenChannel,timeoutMS,retryCount);
        setPayload(payload);
    }

    protected void init(byte sendChannel, byte repeatCount, byte packetDelayMS, byte listenChannel, int timeoutMS, byte retryCount) {
        initWithData(new byte[] {RILEYLINK_CMD_SEND_AND_LISTEN,sendChannel,repeatCount,packetDelayMS,listenChannel,
                (byte)(timeoutMS>>24),(byte)((timeoutMS & 0x00FF0000) >> 16), (byte)((timeoutMS & 0x0000FF00)>>8),(byte)(timeoutMS & 0x000000FF),
                retryCount});
    }

    void setSendChannel(byte sendChannel) {
        data.set(1,sendChannel);
    }
    byte getSendChannel() {
        return byteAt(1);
    }

    void setRepeatCount(byte repeatCount) {
        data.set(2,repeatCount);
    }
    byte getRepeatCount() {
        return byteAt(2);
    }

    void setPacketDelayMS(byte packetDelayMS) {
        data.set(3,packetDelayMS);
    }
    byte getPacketDelayMS() {
        return byteAt(3);
    }

    void setListenChannel(byte listenChannel) {
        data.set(4,listenChannel);
    }
    byte getListenChannel() {
        return byteAt(4);
    }

    void setTimeoutMS(int timeoutMS) {
        data.set(5,(byte)(timeoutMS >> 24));
        data.set(6,(byte)((timeoutMS & 0x00FF0000)>> 16));
        data.set(7,(byte)((timeoutMS & 0x0000FF00)>> 8));
        data.set(8,(byte)((timeoutMS & 0x000000FF)));
    }

    int getTimeoutMS() {
        int rval = 0;
        rval = byteAt(5);
        rval = (rval << 8) | byteAt(6);
        rval = (rval << 8) | byteAt(7);
        rval = (rval << 8) | byteAt(8);
        return rval;
    }

    void setRetryCount(byte retryCount) {
        data.set(9,retryCount);
    }
    byte getRetryCount() {
        return byteAt(9);
    }
    void setPayload(byte[] payload) {
        Log.w(TAG,"setPayload: untested code");
        if (data.size() > 10) {
            data = new ArrayList<>(data.subList(0,9));
        }
        appendBytes(payload);
    }
    byte[] getPayload() {
        return ByteUtil.fromByteArray(data.subList(10,data.size()-1));
    }
}
