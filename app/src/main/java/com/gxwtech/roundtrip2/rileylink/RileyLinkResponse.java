package com.gxwtech.roundtrip2.rileylink;

import com.gxwtech.roundtrip2.Packet;

/**
 * Created by geoff on 4/9/16.
 */
public class RileyLinkResponse extends Packet {
    private static final String TAG = "RileyLinkResponse";

    public static final byte TIMEOUT = (byte) 0xAA;
    public static final byte INTERRUPTED = (byte) 0xBB;
    public static final byte ZERO_DATA = (byte) 0xCC;

    public RileyLinkResponse() {
        super();
    }
    public RileyLinkResponse(byte[] initData) {
        super(initData);
    }

    public boolean isValid() {
        if (data == null) {
            return false;
        }
        if (data.size() == 0) {
            return false;
        }
        return true;
    }

    public byte responseType() {
        return byteAt(0);
    }
}
