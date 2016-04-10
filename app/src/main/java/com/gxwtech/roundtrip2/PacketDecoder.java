package com.gxwtech.roundtrip2;

import android.util.Log;

import com.gxwtech.roundtrip2.rileylink.RileyLinkPacket;
import com.gxwtech.roundtrip2.rileylink.RileyLinkUtil;
import com.gxwtech.roundtrip2.util.ByteUtil;
import com.gxwtech.roundtrip2.util.CRC;

/**
 * Created by geoff on 4/10/16.
 *
 * This decoder is intended as a double-check against all of the various packet-creation
 * code, since packet creation has been parcelled out to various classes and sub-classes and
 * it can get quite complex.
 *
 * It takes *any* packet and attempts to figure out what the heck it is.
 *
 */

/* For Reference: static void System.arraycopy(Object src, int srcPos, Object dest, int destPos, int length) */


public class PacketDecoder {
    private static final String TAG = "PacketDecoder";
    public PacketDecoder() {
    }
    public static void decodePacket(byte[] bytes) {
        decodePacket(new Packet(bytes));
    }
    public static void decodePacket(Packet p) {
        if (p == null) {
            Log.w(TAG,"Packet is null");
            return;
        }
        int len = p.length();
        if (len == 0) {
            Log.w(TAG,"Packet is zero length");
            return;
        }
        byte firstbyte = p.byteAt(0);
        Log.w(TAG,"Decoding "+len+" packet bytes: "+ ByteUtil.shortHexString(p.getRaw()));
        if ((firstbyte == 0) && (len==1)) {
            Log.w(TAG,"Packet is a single null byte -- probably RileyLink response of 'nothing received'");
            return;
        }
        if (len == 2) {
            if (firstbyte == 0xAA) {
                Log.w(TAG,"Packet is a RileyLink response: 'TIMEOUT'");
                return;
            }
            if (firstbyte == 0xBB) {
                Log.w(TAG,"Packet is a RileyLink response: 'INTERRUPTED'");
                return;
            }
            if (firstbyte == 0xCC) {
                Log.w(TAG,"Packet is a RileyLink response: 'ZERO_DATA'");
                return;
            }
        }
        if (firstbyte == len-1) {
            Log.w(TAG,"Packet is probably RileyLink command.");
            decodeRileyLinkCommandPacket(p);
        }
        Log.w(TAG,"Unknown packet. clues? "+ByteUtil.showPrintable(p.getRaw()));

    }

    public static void decodeRileyLinkCommandPacket(Packet p) {
        int len = p.length();
        byte rlCommand = p.byteAt(1);
        String commandDesc = RileyLinkPacket.getDescriptionForCommandByte(rlCommand);
        Log.w(TAG,"RileyLink command type: "+commandDesc);
        switch (rlCommand) {
            case RileyLinkPacket.RILEYLINK_CMD_GET_PACKET:
                // byte channel, int timeout_ms
                if (len < 7) {
                    Log.w(TAG,"Packet is truncated. Should be at least 7 bytes.");
                }
                if (len > 2) {
                    byte listenChannel = p.byteAt(2);
                    Log.w(TAG, "Listen on channel " + listenChannel);
                    if (len > 6) {
                        int timeoutMS = (p.byteAt(3) << 24) + (p.byteAt(4) << 16) + (p.byteAt(5)<<8) + (p.byteAt(6));
                        Log.w(TAG,"Timeout is "+timeoutMS+" milliseconds.");
                    }
                }
                break;
            case RileyLinkPacket.RILEYLINK_CMD_GET_STATE:
                Log.e(TAG,"*** Not yet implemented ***");
                break;
            case RileyLinkPacket.RILEYLINK_CMD_GET_VERSION:
                /* no further data */
                break;
            case RileyLinkPacket.RILEYLINK_CMD_RESET:
                Log.e(TAG,"*** Not yet implemented ***");
                break;
            case RileyLinkPacket.RILEYLINK_CMD_SEND_PACKET:
                /*
                  uint8_t channel;
                  uint8_t repeat_count;
                  uint8_t delay_ms;
                 */
                if (len < 5) {
                    Log.w(TAG,"Packet is truncated.  Should be at least 5 bytes.");
                }
                if (len > 2) {
                    byte sendChannel = p.byteAt(2);
                    Log.w(TAG,"Send Channel "+sendChannel);
                }
                if (len > 3) {
                    byte repeat_count = p.byteAt(3);
                    Log.w(TAG,"Repeat Count "+(repeat_count<0? repeat_count+256 : repeat_count));
                }
                if (len > 4) {
                    byte delay_ms = p.byteAt(4);
                    Log.w(TAG,"Delay between sends: "+delay_ms);
                }
                if (len > 5) {
                    byte[] payload = new byte[len - 5];
                    System.arraycopy(p.getRaw(),5,payload,0,len-5);
                    Log.w(TAG,"Payload is: " + ByteUtil.shortHexString(payload));
                    Log.w(TAG,"Assuming payload is Minimed message");
                    decodeMinimedPayload(payload);
                } else {
                    Log.w(TAG,"RileyLink send packet command has no payload");
                }
                break;
            case RileyLinkPacket.RILEYLINK_CMD_SEND_AND_LISTEN:
            case RileyLinkPacket.RILEYLINK_CMD_UPDATE_REGISTER:
                Log.e(TAG,"*** Not yet implemented ***");
                break;
            default:
                break;
        }
    }

    public static void decodeMinimedPayload(byte[] ra) {
        if (ra == null) {
            Log.e(TAG,"Null payload?");
            return;
        }
        if (ra.length == 0) {
            Log.w(TAG,"empty payload?");
            return;
        }
        byte[] dec = RileyLinkUtil.decodeRF(ra);
        if (dec == null) {
            Log.e(TAG,"RileyLinkUtil.decodeRF failed");
            return;
        }
        if (dec.length == 0) {
            Log.e(TAG,"decoded RF packet has zero length");
            return;
        }
        Log.w(TAG,"Decoded RF Minimed payload: "+ByteUtil.shortHexString(dec));
        byte minimedAttention = dec[0];
        if (dec.length < 7) {
            Log.w(TAG,String.format("Truncated minimed packet.  length is %d bytes, should be at least 7 bytes",dec.length));
            return;
        }
        byte[] minimedAddress = new byte[] { dec[1], dec[2], dec[3] };
        byte minimedOpcode = dec[4];
        Log.w(TAG,String.format("Opcode %d(0x%02x): %s",minimedOpcode,minimedOpcode,MedtronicCommandEnum.desc(minimedOpcode)));
        byte payloadCRC = dec[dec.length-1];
        byte crc = CRC.crc8(dec,dec.length-1);
        if (crc == payloadCRC) {
            Log.w(TAG,"CRC is OK");
        } else {
            Log.w(TAG,String.format("CRC bad: packet has 0x%02X, should be 0x%02X",payloadCRC,crc));
        }
    }
}
