package com.gxwtech.roundtrip2;

/**
 * Created by geoff on 4/8/16.
 */
public class PacketType {
    public static final byte INVALID = (byte)0xff;

    public static final byte Sentry = (byte)0xa2;
    public static final byte Meter = (byte)0xa5;
    public static final byte Carelink = (byte)0xa7;
    public static final byte Sensor = (byte)0xa8;
}
