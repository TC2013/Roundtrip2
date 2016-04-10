package com.gxwtech.roundtrip2;

import android.util.Log;

import com.gxwtech.roundtrip2.message.MessageType;
import com.gxwtech.roundtrip2.rileylink.RileyLinkPacket;
import com.gxwtech.roundtrip2.rileylink.RileyLink;
import com.gxwtech.roundtrip2.rileylink.RileyLinkUtil;
import com.gxwtech.roundtrip2.util.ByteUtil;

/**
 * Created by geoff on 4/8/16.
 */
public class Minimed {
    public static final int STANDARD_PUMP_RESPONSE_WINDOW = 180;
    public static final int EXPECTED_MAX_BLE_LATENCY_MS = 1500;
    private static final String TAG = "Minimed";
    protected String pumpId;
    protected boolean mAwake;
    protected RileyLink mRileyLink;
    public Minimed() {
        pumpId = "518163";
        mAwake = false;
    }

    public String getPumpId() {
        return pumpId;
    }

    public byte[] getPumpIdBytes() { return ByteUtil.fromHexString(getPumpId()); }

    public void setPumpId(String pumpId) {
        this.pumpId = pumpId;
    }

    public boolean isAwake() {
        return mAwake;
    }

    public RileyLink getRileyLink() {
        return mRileyLink;
    }

    public void setRileyLink(RileyLink RileyLink) {
        mRileyLink = RileyLink;
    }

    public MinimedCommandPacket createCommand(MedtronicCommandEnum commandType, String args) {
        MinimedCommandPacket rval = new MinimedCommandPacket(getPumpIdBytes(),commandType.toByte(),ByteUtil.fromHexString(args));
        return rval;
    }

    public MinimedCommandPacket powerMessage() {
        return powerMessageWithArgs("00");
    }

    public MinimedCommandPacket powerMessageWithArgs(String args) {
        return createCommand(MedtronicCommandEnum.CMD_M_POWER_CTRL,args);
    }

    public MinimedCommandPacket buttonPressMessage() {
        return buttonPressMessageWithArgs("00");
    }

    public MinimedCommandPacket buttonPressMessageWithArgs(String args) {
        return createCommand(MedtronicCommandEnum.CMD_M_KEYPAD_PUSH,args);
    }

    public MinimedCommandPacket batteryStatusMessage() {
        return createCommand(MedtronicCommandEnum.CMD_M_READ_BATTERY_VOLTAGE,"00");
    }

    public MinimedPacket sendAndListen(byte[] msg, int timeoutMS, int repeat, int msBetweenPackets, int retryCount) {
        byte[] pkt = new byte[]{RileyLinkPacket.RILEYLINK_CMD_SEND_AND_LISTEN};
        byte sendChannel = 0;
        byte listenChannel = 0;
        pkt = ByteUtil.concat(pkt,new byte[] {sendChannel,(byte)repeat,(byte)msBetweenPackets,listenChannel,
                ByteUtil.highByte((short)timeoutMS),ByteUtil.lowByte((short)timeoutMS),(byte)retryCount});
        pkt = ByteUtil.concat(pkt,RileyLinkUtil.encodeData(RileyLinkUtil.appendChecksum(msg)));
        int totalTimeout = repeat * msBetweenPackets + timeoutMS + EXPECTED_MAX_BLE_LATENCY_MS;
        byte[] response = mRileyLink.writeAndReadData(pkt);
        //byte[] response = mRileyLink.doCmd(cmd,totalTimeout);
        if ((response!=null) && (response.length > 2)) {
            // we got something!
            Log.e(TAG, "sendAndListen: received: " + ByteUtil.shortHexString(response));
            MinimedPacket rval = new MinimedPacket();
            rval.initFromRadioData(response);
            return rval;
        }
        Log.e(TAG,"sendAndListen: no response(?)");
        return null;
    }

    public void checkResponseCount() {
        byte[] responseCount = mRileyLink.readResponseCount();
        if (responseCount == null) {
            Log.e(TAG,"checkResponseCount: NULL!");
            return;
        }
        Log.e(TAG,"checkResponseCount: "+ByteUtil.shortHexString(responseCount));
    }

    public boolean wakeup(int durationMinutes) {
        Log.d(TAG,"Wakey-Wakey!");
        checkResponseCount();
        MinimedPacket pkt = sendAndListen(powerMessage().getBytestream(),15000,200,0,0);
        if (pkt != null) {
            if (pkt.getMessageType() == MessageType.ACK) {
                Log.e(TAG,"Pump is already awake (Pump sent ACK)");
            }
        } else {
            // send power on message
            Log.d(TAG,"Attempting to wake pump.");
            String wakeupString = "0201"+String.format("%02X",((byte)durationMinutes))+"0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";

            pkt = sendAndListen(powerMessageWithArgs(wakeupString).getBytestream(),STANDARD_PUMP_RESPONSE_WINDOW,0,0,3);
            if (pkt != null) {
                if (pkt.getMessageType() == MessageType.ACK) {
                    Log.e(TAG,"Pump ACK'd power on for "+durationMinutes+" minutes.");
                    // TODO: remember when pump will no longer be awake.
                }
            } else {
                Log.d(TAG,"No response(?)");
                checkResponseCount();
                return false;
            }
        }
        return true;
    }


}
