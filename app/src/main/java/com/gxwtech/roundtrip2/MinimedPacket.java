package com.gxwtech.roundtrip2;

import com.gxwtech.roundtrip2.message.MessageType;
import com.gxwtech.roundtrip2.rileylink.RileyLinkUtil;
import com.gxwtech.roundtrip2.util.ByteUtil;
import com.gxwtech.roundtrip2.util.CRC;

/**
 * Created by geoff on 4/8/16.
 *
 */
public class MinimedPacket extends Packet {
    protected int rssi;
    protected int packetNumber;

    public MinimedPacket() {
        super();
    }

    public void initFromRadioData(byte[] data) {
        if (data.length > 0) {
            byte rssiDec = data[0];
            byte rssiOffset = 73;
            if (rssiDec >= 128) {
                this.rssi = (short)((short)(rssiDec-256)/2) - rssiOffset;
            } else {
                this.rssi = (rssiDec/2) - rssiOffset;
            }
        }
        if (data.length > 1) {
            this.packetNumber = data[1];
        }
        if (data.length > 2) {
            initWithData(RileyLinkUtil.decodeRF(ByteUtil.substring(data,2,data.length-2)));
        }
    }
    public boolean isCrcValid() {
        byte packetCrc = byteAt(data.size()-1);
        byte crc = CRC.crc8(ByteUtil.substring(getRaw(),0,data.size()-1));
        return (packetCrc == crc);
    }

    public boolean isValid() {
        if (data == null) {
            return false;
        }
        return ((data.size() > 0) && isCrcValid());
    }

    public byte getMessageType() {
        if (!isValid()) {
            return MessageType.INVALID;
        }
        Byte mt = byteAt(4);
        if (mt == null) {
            return MessageType.INVALID;
        }
        return mt;
    }
    public String getAddress() {
        return String.format("%02X%02X%02X",byteAt(1),byteAt(2),byteAt(3));
    }
}
