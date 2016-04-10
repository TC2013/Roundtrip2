package com.gxwtech.roundtrip2;

import android.util.Log;

import com.gxwtech.roundtrip2.message.MessageType;
import com.gxwtech.roundtrip2.rileylink.RLPacket_Listen;
import com.gxwtech.roundtrip2.rileylink.RLPacket_Send;
import com.gxwtech.roundtrip2.rileylink.RLPacket_SendAndListen;
import com.gxwtech.roundtrip2.rileylink.RileyLinkPacket;
import com.gxwtech.roundtrip2.rileylink.RileyLink;
import com.gxwtech.roundtrip2.rileylink.RileyLinkResponse;
import com.gxwtech.roundtrip2.rileylink.RileyLinkUtil;
import com.gxwtech.roundtrip2.util.ByteUtil;

import java.util.ArrayList;
import java.util.List;

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
        RLPacket_SendAndListen pkt = new RLPacket_SendAndListen((byte)0,(byte)repeat,(byte)msBetweenPackets,(byte)0,timeoutMS,(byte)retryCount,msg);

        int totalTimeout = repeat * msBetweenPackets + timeoutMS + EXPECTED_MAX_BLE_LATENCY_MS;
        byte[] response = mRileyLink.writeAndReadData(pkt.getBytestream(),totalTimeout);

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

    public void sendToMinimed(byte[] msg, int channel, int repeatCount, int delayMS) {
        Log.d(TAG,"sendToMinimed: payload is "+ByteUtil.shortHexString(msg));
        byte[] payloadWithChecksum = RileyLinkUtil.appendChecksum(msg);
        Log.d(TAG,"sendToMinimed: payload with checksum: "+ByteUtil.shortHexString(payloadWithChecksum));
        byte[] encoded = RileyLinkUtil.encodeData(payloadWithChecksum);
        Log.d(TAG,"sendToMinimed: encoded payload is: "+ ByteUtil.shortHexString(encoded));
        //byte[] encodedNullAppended = ByteUtil.concat(encoded,(byte)0);
        RLPacket_Send pkt = new RLPacket_Send((byte)channel, (byte)repeatCount,(byte)delayMS,encoded);
        int totalTimeout = repeatCount * delayMS + EXPECTED_MAX_BLE_LATENCY_MS;
        Log.d(TAG,"sendToMinimed: data to send: "+ByteUtil.shortHexString(pkt.getBytestream()));
        mRileyLink.writeToData(pkt.getBytestream(),totalTimeout);
    }

    public void orderRileyLinkToListen(int listenChannel, int timeoutMS) {
        RLPacket_Listen pkt = new RLPacket_Listen((byte)listenChannel,timeoutMS);
        mRileyLink.writeToData(pkt.getBytestream(),timeoutMS); // two different uses of timeoutMS here!
    }

    public MinimedPacket waitForResponse(int timeoutMS) {
        MinimedPacket rval = new MinimedPacket();
        byte[] response = mRileyLink.readFromData(timeoutMS);
        if (response == null) {
            Log.e(TAG,"waitForResponse: NULL response");
        } else {
            if (response.length == 0) {
                Log.e(TAG,"waitForResponse: EMPTY response");
            } else {
                RileyLinkResponse rlResponse = new RileyLinkResponse(response);
                if (rlResponse.responseType() == RileyLinkResponse.INTERRUPTED) {
                    Log.e(TAG,"RileyLink was interrupted");
                } else if (rlResponse.responseType() == RileyLinkResponse.TIMEOUT) {
                    Log.e(TAG,"RileyLink reports timeout");
                } else if (rlResponse.responseType() == RileyLinkResponse.ZERO_DATA) {
                    Log.e(TAG,"RileyLink reports ZERO_DATA");
                } else if (rlResponse.responseType() == RileyLinkResponse.EMPTY_PACKET) {
                    Log.e(TAG,"RileyLink reports No Response From Pump");
                } else {
                    Log.w(TAG,"waitForResponse: got something: "+ ByteUtil.shortHexString(response));
                }
                rval.initFromRadioData(response);
            }
        }
        return rval;
    }

    public void checkResponseCount() {
        byte[] responseCount = mRileyLink.readResponseCount(500);
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

    void scanForPump() {
        List<Float> frequencies = new ArrayList<>();
        wakeup(5);
    }

}
