package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpTimeStamp;

abstract public class Record {
    private static final String TAG = "Record";
    protected byte recordOp;
    protected int length;
    protected Bundle mDictionaryRepresentation = new Bundle();
    protected String recordTypeName = this.getClass().getSimpleName();

    public String getRecordTypeName() {
        return recordTypeName;
    }

    public Record() {
        length = 1;
    }

    public boolean parseFrom(byte[] data, PumpModel model) {
        if (data == null) {
            return false;
        }
        if (data.length < 1) {
            return false;
        }
        recordOp = data[0];
        return true;
    }

    public PumpTimeStamp getTimestamp() {
        return new PumpTimeStamp();
    }

    public int getLength() {
        return length;
    }

    public byte getRecordOp() {
        return recordOp;
    }

    protected static int asUINT8(byte b) {
        return (b < 0) ? b + 256 : b;
    }

    protected void addValue(Bundle b, String key, int intValue) {
        b.putInt(key, intValue);
    }

    protected void addValue(Bundle b, String key, double doubleValue) {
        b.putDouble(key, doubleValue);
    }

    protected void addValue(Bundle b, String key, String stringValue) {
        mDictionaryRepresentation.putString(key, stringValue);
    }

    protected void addValue(Bundle b, String key, PumpTimeStamp timestampValue) {
        mDictionaryRepresentation.putSerializable(key, timestampValue.getLocalDateTime());
    }

    protected void addValue(Bundle b, String key, byte[] byteArray) {
        mDictionaryRepresentation.putByteArray(key, byteArray);
    }

    protected void addValue(Bundle b, String key, boolean truthValue) {
        mDictionaryRepresentation.putBoolean(key, truthValue);
    }

    protected void addValue(Bundle b, String key, byte byteVal) {
        mDictionaryRepresentation.putByte(key,byteVal);
    }

    public Bundle dictionaryRepresentation() {
        Bundle rval = new Bundle();
        writeToBundle(rval);
        return rval;
    }

    public boolean readFromBundle(Bundle in) {
        // length is determined at instantiation
        // record type name is "static"
        // opcode has already been read.
        return true;
    }

    public void writeToBundle(Bundle in) {
        in.putInt("length",length);
        in.putInt("_opcode",recordOp);
        in.putString("_type",getRecordTypeName());
    }

}
