package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData;

/**
 * Created by geoff on 5/13/15.
 *
 * This class was taken from medtronic-android-uploader.
 * This class was written such that the constructors did all the work, which resulted
 * in annoyances such as exceptions during constructors.
 *
 * TODO: This class needs to be revisited and probably rewritten. (2016-06-12)
 *
 */

import android.os.Bundle;
import android.util.Log;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records.BolusWizardBolusEstimatePumpEvent;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records.Record;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records.RecordTypeEnum;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records.TempBasalDurationPumpEvent;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records.TempBasalRatePumpEvent;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;
import com.gxwtech.roundtrip2.util.ByteUtil;
import com.gxwtech.roundtrip2.util.CRC;
import com.gxwtech.roundtrip2.util.HexDump;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Page {
    private final static String TAG = "Page";
    private static final boolean DEBUG_PAGE = false;

    private byte[] crc;
    private byte[] data;
    protected PumpModel model;
    public List<Record> mRecordList;

    public Page() {
        this.model = PumpModel.UNSET;
        mRecordList = new ArrayList<>();
    }

    public byte[] getRawData() {
        if (data == null) {
            return crc;
        }
        if (crc == null) {
            return data;
        }
        return ByteUtil.concat(data,crc);
    }

    public boolean parseFrom(byte[] rawPage, PumpModel model) {
        mRecordList = new ArrayList<>(); // wipe old contents each time when parsing.
        if (rawPage.length != 1024) {
            Log.e(TAG,"Unexpected page size. Expected: 1024 Was: " + rawPage.length);
            return false;
        }
        this.model = model;
        if (DEBUG_PAGE) {
            Log.i(TAG, "Parsing page");
        }
        this.data = Arrays.copyOfRange(rawPage, 0, 1022);
        this.crc = Arrays.copyOfRange(rawPage, 1022, 1024);
        byte[] expectedCrc = CRC.calculate16CCITT(this.data);
        if (DEBUG_PAGE) {
            Log.i(TAG, String.format("Data length: %d", data.length));
        }
        if (!Arrays.equals(crc, expectedCrc)) {
            Log.w(TAG, String.format("CRC does not match expected value. Expected: %s Was: %s", HexDump.toHexString(expectedCrc), HexDump.toHexString(crc)));
        } else {
            if (DEBUG_PAGE) {
                Log.i(TAG, "CRC OK");
            }
        }

        // Go through page, parsing what we can
        // add records to recordList
        // Records can be of variable size, so ask the record how large it is.
        // GGW: because (I think) our record-sizes are incorrect, this parser can't find anything.
        // See below for alternate parser
        /*
        int dataIndex = 0;
        boolean done = false;
        while(!done) {
            Record record = attemptParseRecord(data, dataIndex);
            if (record != null) {
                mRecordList.add(record);
                Log.d(TAG,String.format("Found record %s at index %d",
                        record.getClass().getSimpleName(),dataIndex));
                // old code stopped when it encountered a 0x00 where it expected a record to start.
                dataIndex = dataIndex + record.getSize(); // jump to next record
            } else {
                Log.d(TAG,String.format("No record found for bytecode 0x%02X",data[dataIndex]));
                // if we failed to form a record, quit.
                done = true;
            }
            if (dataIndex >= data.length) {
                // if we've hit the end of our page, quit.
                done = true;
            } else if (data[dataIndex] == 0x00) {
                // if the next 'record' starts with a zero, quit.
                Log.d(TAG,String.format("Found end of Records(0x00) at index %d",dataIndex));
                done = true;
            }
        }
        */
        // GGW: The above parser fails, so we're going to hack it:
        // Find possible matches for TempBasalRatePumpEvent, TempBasalDurationPumpEvent and BolusWizardBolusEstimatePumpEvent events
        // (as those are the only ones we care about at the moment. and try to parse them.
        int dataIndex = 0;
        boolean done = false;
        while (!done) {
            Record record = null;
            if (data[dataIndex] != 0) {
                // just don't bother, if the data byte is zero.
                /*
                Log.d(TAG,String.format("Attempting to parse record at offset %d, OpCode is 0x%02X",
                        dataIndex,data[dataIndex]));
                        */
                record = attemptParseRecord(data,dataIndex);
            }
            if (record != null) {
                Log.v(TAG,"parseFrom: found event "+record.getClass().getSimpleName());
                // found something.  Is it something we trust or care about?
                if (record.getRecordOp() == RecordTypeEnum.RECORD_TYPE_BolusWizardBolusEstimate.opcode()) {
                    BolusWizardBolusEstimatePumpEvent bw = (BolusWizardBolusEstimatePumpEvent)record;
                    mRecordList.add(record);
                    /*
                    Log.d(TAG,String.format("Found BolusWizardBolusEstimatePumpEvent record (time:%s) at offset %d",
                            bw.getTimeStamp().toString(),dataIndex));
                            */
                    if (record.getLength() > 0) {
                        dataIndex += record.getLength();
                    } else {
                        dataIndex +=1;
                    }
                } else if (record.getRecordOp() == RecordTypeEnum.RECORD_TYPE_TEMPBASALDURATION.opcode()) {
                    TempBasalDurationPumpEvent tbd = (TempBasalDurationPumpEvent) record;
                    mRecordList.add(record);
                    /*
                    Log.d(TAG,String.format("Found TempBasalDurationPumpEvent record (time:%s) offset %d, duration %d",
                            tbd.getTimeStamp(),dataIndex,tbd.durationMinutes));
                            */
                    if (record.getLength() > 0) {
                        dataIndex += record.getLength();
                    } else {
                        dataIndex +=1;
                    }
                } else if (record.getRecordOp() == RecordTypeEnum.RECORD_TYPE_TEMPBASALRATE.opcode()) {
                    TempBasalRatePumpEvent tbr = (TempBasalRatePumpEvent) record;
                    mRecordList.add(record);
                    /*
                    Log.d(TAG,String.format("Found TempBasalRatePumpEvent record (time:%s) offset %d, rate %.3f",
                            tbr.getTimeStamp(),dataIndex,tbr.basalRate));
                            */
                    if (record.getLength() > 0) {
                        dataIndex += record.getLength();
                    } else {
                        dataIndex +=1;
                    }
                } else {
                    mRecordList.add(record); // add it anyway.
                    dataIndex +=1;

                }
            } else {
                dataIndex +=1;
            }
            if (dataIndex >= data.length - 2) {
                done = true;
            }
        }
        if (DEBUG_PAGE) {
            Log.i(TAG, String.format("Number of records: %d", mRecordList.size()));
            int index = 1;
            for (Record r : mRecordList) {
                Log.v(TAG, String.format("Record #%d: %s", index,r.getShortTypeName()));
                index += 1;
            }
        }
        return true;
    }

    /* attemptParseRecord will attempt to create a subclass of Record from the given
     * data and offset.  It will return NULL if it fails.  If it succeeds, the returned
     * subclass of Record can be examined for its length, so that the next attempt can be made.
     */
    public static <T extends Record> T attemptParseRecord(byte[] data, int offsetStart) {
        // no data?
        if (data == null) {
            return null;
        }
        // invalid offset?
        if (data.length < offsetStart) {
            return null;
        }
        //Log.d(TAG,String.format("checking for handler for record type 0x%02X at index %d",data[offsetStart],offsetStart));
        RecordTypeEnum en = RecordTypeEnum.fromByte(data[offsetStart]);
        T record = en.getRecordClassInstance();
        if (record != null) {
            // have to do this to set the record's opCode
            byte[] tmpData = new byte[data.length];
            System.arraycopy(data, offsetStart, tmpData, 0, data.length - offsetStart);
            boolean didParse = record.parseFrom(tmpData, PumpModel.MM522);
            if (!didParse) {
                Log.e(TAG,String.format("attemptParseRecord: class %s (opcode 0x%02X) failed to parse at offset %d",record.getShortTypeName(),data[offsetStart],offsetStart));
            }
        }
        return record;
    }

    public static DateTime parseSimpleDate(byte[] data, int offset) {
        DateTime timeStamp = null;
        int seconds = 0;
        int minutes = 0;
        int hour = 0;
        //int high = data[0] >> 4;
        int low = data[0 + offset] & 0x1F;
        //int year_high = data[1] >> 4;
        int mhigh = (data[0 + offset] & 0xE0) >> 4;
        int mlow = (data[1 + offset] & 0x80) >> 7;
        int month = mhigh + mlow;
        int dayOfMonth = low + 1;
        // python code says year is data[1] & 0x0F, but that will cause problem in 2016.
        // Hopefully, the remaining bits are part of the year...
        int year = data[1 + offset] & 0x3F;
        /*
        Log.w(TAG, String.format("Attempting to create DateTime from: %04d-%02d-%02d %02d:%02d:%02d",
                year + 2000, month, dayOfMonth, hour, minutes, seconds));
         */
        try {
            timeStamp = new DateTime(year + 2000, month, dayOfMonth, hour, minutes, seconds);
        } catch (org.joda.time.IllegalFieldValueException e) {
            //Log.e(TAG,"Illegal DateTime field");
            //e.printStackTrace();
            return null;
        }
        return timeStamp;
    }

    public static void discoverRecords(byte[] data) {
        int i = 0;
        boolean done = false;

        ArrayList<Integer> keyLocations= new ArrayList();
        while (!done) {
            RecordTypeEnum en = RecordTypeEnum.fromByte(data[i]);
            if (en != RecordTypeEnum.RECORD_TYPE_NULL) {
                keyLocations.add(i);
                Log.v(TAG, String.format("Possible record of type %s found at index %d", en, i));
            }
            /*
            DateTime ts = parseSimpleDate(data,i);
            if (ts != null) {
                if (ts.year().get() == 2015) {
                    Log.w(TAG, String.format("Possible simple date at index %d", i));
                }
            }
            */
            i = i + 1;
            done = (i >= data.length-2);
        }
        // for each of the discovered key locations, attempt to parse a sequence of records
        for(RecordTypeEnum en : RecordTypeEnum.values()) {

        }
        for (int ix = 0; ix < keyLocations.size(); ix++) {

        }
    }

    /*
    *
    * For IPC serialization
    *
     */

    /*
    private byte[] crc;
    private byte[] data;
    protected PumpModel model;
    public List<Record> mRecordList;
    */

    public Bundle pack() {
        Bundle bundle = new Bundle();
        bundle.putByteArray("crc",crc);
        bundle.putByteArray("data",data);
        bundle.putString("model",PumpModel.toString(model));
        ArrayList<Bundle> records = new ArrayList<>();
        for (int i=0; i<mRecordList.size(); i++) {
            records.add(mRecordList.get(i).dictionaryRepresentation());
        }
        bundle.putParcelableArrayList("mRecordList",records);
        return bundle;
    }

    public void unpack(Bundle in) {
        crc = in.getByteArray("crc");
        data = in.getByteArray("data");
        model = PumpModel.fromString(in.getString("model"));
        ArrayList<Bundle> records = in.getParcelableArrayList("mRecordList");
        mRecordList = new ArrayList<>();
        if (records != null) {
            for (int i=0; i<records.size(); i++) {
                Record r = RecordTypeEnum.getRecordClassInstance(records.get(i));
                r.readFromBundle(records.get(i));
                mRecordList.add(r);
            }
        }
    }


}