package com.gxwtech.roundtrip2.rileylink;

import android.util.Log;

import com.gxwtech.roundtrip2.util.ByteUtil;

import java.util.ArrayList;

/**
 * Created by geoff on 4/9/16.
 */
public class RLPacket_Send extends RileyLinkPacket {
    private static final String TAG = "RLPacket_Send";
    public RLPacket_Send() {
        super(new byte[] {RILEYLINK_CMD_SEND_PACKET,2,0,0});
    }

    protected void init() {
        init((byte)2,(byte)0,(byte)0);
    }

    public RLPacket_Send(byte sendChannel, byte repeatCount, byte packetDelayMS) {
        super();
        init(sendChannel,repeatCount,packetDelayMS);
    }

    public RLPacket_Send(byte[] payload) {
        init();
        setPayload(payload);
    }

    public RLPacket_Send(byte sendChannel, byte repeatCount, byte packetDelayMS, byte[] payload) {
        super();
        init(sendChannel,repeatCount,packetDelayMS);
        setPayload(payload);
    }

    protected void init(byte sendChannel, byte repeatCount, byte packetDelayMS) {
        initWithData(new byte[] {RILEYLINK_CMD_SEND_PACKET,sendChannel,repeatCount,packetDelayMS});
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

    void setPayload(byte[] payload) {
        Log.w(TAG,"setPayload: untested code");
        if (data.size() > 4) {
            data = new ArrayList<>(data.subList(0,3));
        }
        appendBytes(payload);
    }
    byte[] getPayload() {
        return ByteUtil.fromByteArray(data.subList(4,data.size()-1));
    }
}
