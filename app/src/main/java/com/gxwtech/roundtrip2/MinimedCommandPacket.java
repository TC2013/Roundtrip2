package com.gxwtech.roundtrip2;

import com.gxwtech.roundtrip2.util.ByteUtil;

/**
 * Created by geoff on 4/8/16.
 */
public class MinimedCommandPacket extends Packet {
    public MinimedCommandPacket(byte[] pumpId, byte commandCode, byte[] args) {
        byte[] toInit = new byte[] { (byte)0xa7 };
        toInit = ByteUtil.concat(toInit,pumpId);
        toInit = ByteUtil.concat(toInit,commandCode);
        toInit = ByteUtil.concat(toInit,args);
        initWithData(toInit);
    }
    public byte[] getBytestream() {
        return getRaw();
    }
}
