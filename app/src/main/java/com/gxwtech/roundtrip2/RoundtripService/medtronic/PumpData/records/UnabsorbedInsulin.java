package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;


import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

import java.util.ArrayList;

public class UnabsorbedInsulin extends Record {

    class UnabsorbedInsulinRecord {
        double amount = 0.0;
        int age = 0;
        public UnabsorbedInsulinRecord(double amount, int age) {
            this.amount = amount;
            this.age = age;
        }
    }

    ArrayList<UnabsorbedInsulinRecord> records = new ArrayList<>();

    public UnabsorbedInsulin() {
    }

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        length = asUINT8(data[1]);
        if (length < 2) {
            length = 2;
        }
        if (length > data.length) {
            return false;
        }
        int numRecords = (asUINT8(data[1]) - 2) / 3;
        for (int i=0; i<numRecords; i++) {
            double amount = (double)(asUINT8(data[2 + (i * 3)])) / 40.0;
            int age = asUINT8(data[3 + (i*3)]) + (((asUINT8(data[4 + (i*3)])) & 0b110000) << 4);
            records.add(new UnabsorbedInsulinRecord(amount,age));
        }
        addValue("data",records);
        return true;
    }
}
