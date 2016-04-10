package com.gxwtech.roundtrip2;

import com.gxwtech.roundtrip2.util.ByteUtil;

import java.util.ArrayList;

/**
 * Created by geoff on 4/9/16.
 */
/*
   A packet is a base class that just contains an array of bytes.
 */
public class Packet {
    protected ArrayList<Byte> data = new ArrayList<>();
    public Packet() {}
    public Packet(byte[] data) {
        initWithData(data);
    }
    public void initWithData(byte[] data) {
        if (data != null) {
            this.data = ByteUtil.toByteArray(data);
        }
    }
    public Byte byteAt(int index) {
        if ((index < 0) || (index>=data.size())) {
            return null;
        }
        return data.get(index);
    }
    public byte[] getRaw() {
        return ByteUtil.fromByteArray(data);
    }
    public ArrayList<Byte> getData() { return data; }

    public void appendBytes(byte[] bytes) {
        for (int i=0; i<bytes.length; i++) {
            data.add(bytes[i]);
        }
    }
    public int size() {
        return data.size();
    }
    public int length() {
        return size();
    }
}
