package com.gxwtech.roundtrip2.message;

/**
 * Created by geoff on 4/8/16.
 */
public class MessageType {
    public static final byte INVALID = (byte) 0xff;

    public static final byte ALERT = (byte) 0x01;
    public static final byte ALERT_CLEARED = (byte) 0x02;
    public static final byte DEVICE_TEST = (byte)0x03;
    public static final byte PUMP_STATUS = (byte)0x04;
    public static final byte ACK = (byte)0x06;
    public static final byte BACKFILL = (byte)0x08;
    public static final byte FIND_DEVICE = (byte)0x09;
    public static final byte DEVICE_LINK = (byte)0x0a;
    public static final byte PUMP_DUMP = (byte)0x0a;
    public static final byte POWER = (byte)0x5d;
    public static final byte BUTTON_PRESS = (byte)0x5b;
    public static final byte GET_PUMP_MODEL = (byte)0x8d;
    public static final byte GET_BATTERY = (byte)0x72;
    public static final byte READ_HISTORY = (byte)0x80;
}