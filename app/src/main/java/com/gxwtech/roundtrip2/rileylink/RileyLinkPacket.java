package com.gxwtech.roundtrip2.rileylink;

import android.util.Log;

import com.gxwtech.roundtrip2.MinimedPacket;
import com.gxwtech.roundtrip2.Packet;
import com.gxwtech.roundtrip2.util.ByteUtil;

/**
 * Created by geoff on 4/2/16.
 */
public class RileyLinkPacket extends Packet {
    private static final String TAG = "RileyLinkPacket";

    public static final byte RILEYLINK_CMD_INVALID = 0;
    public static final byte RILEYLINK_CMD_GET_STATE = 1;
    public static final byte RILEYLINK_CMD_GET_VERSION = 2;
    public static final byte RILEYLINK_CMD_GET_PACKET = 3;
    public static final byte RILEYLINK_CMD_SEND_PACKET = 4;
    public static final byte RILEYLINK_CMD_SEND_AND_LISTEN = 5;
    public static final byte RILEYLINK_CMD_UPDATE_REGISTER = 6;
    public static final byte RILEYLINK_CMD_RESET = 7;

    /* construct empty */
    public RileyLinkPacket() {
        super();
    }
    /* construct with specific data in packet */
    public RileyLinkPacket(byte [] initData) {
        initWithData(initData);
    }
    /* construct with a single command (RILEYLINK_CMD_...) */
    public RileyLinkPacket(byte ctype) {
        initWithData(new byte[]{ctype});
    }

    public byte getRLPacketType() {
        if (data == null) {
            return RILEYLINK_CMD_INVALID;
        }
        if (data.size() == 0) {
            return RILEYLINK_CMD_INVALID;
        }
        return byteAt(0);
    }

    public byte[] getBytestream() {
        return getRaw();
    }

}
