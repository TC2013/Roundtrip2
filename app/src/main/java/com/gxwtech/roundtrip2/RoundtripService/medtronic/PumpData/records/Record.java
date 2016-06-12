package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpTimeStamp;

import java.util.HashMap;

abstract public class Record {
    private static final String TAG = "Record";
    protected byte recordOp;
    protected int length;
    protected HashMap<String,PumpHistoryDataObject> mDictionaryRepresentation = new HashMap<>();

    protected String recordTypeName = this.getClass().getSimpleName();
    public String getRecordTypeName() {return recordTypeName;}

    public Record() {
        length = 1;
        addValue("_type",getRecordTypeName());
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

    public int getLength() { return length; }

    public byte getRecordOp() {
        return recordOp;
    }

    protected static int asUINT8(byte b) { return (b<0)?b+256:b;}

    protected void addValue(String name, Object value) {
        mDictionaryRepresentation.put(name,new PumpHistoryDataObject(value));
    }

    // derived classes override this to provide a representation of the contents of the event.
    public HashMap<String,PumpHistoryDataObject> dictionaryRepresentation() {
        return mDictionaryRepresentation;
    }
}
