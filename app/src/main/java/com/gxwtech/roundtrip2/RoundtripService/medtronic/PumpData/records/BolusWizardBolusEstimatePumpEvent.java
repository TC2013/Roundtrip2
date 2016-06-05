package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

public class BolusWizardBolusEstimatePumpEvent extends TimeStampedRecord {
    private final static String TAG = "BolusWizardBolusEstimatePumpEvent";

    private int carbohydrates;
    private int bloodGlucose;
    private double foodEstimate;
    private double correctionEstimate;
    private double bolusEstimate;
    private double unabsorbedInsulinTotal;
    private int bgTargetLow;
    private int bgTargetHigh;
    private int insulinSensitivity;
    private double carbRatio;

    public BolusWizardBolusEstimatePumpEvent() {
        correctionEstimate = (double)0.0;
        bloodGlucose = 0;
        carbohydrates = 0;
        carbRatio = 0.0;
        insulinSensitivity = 0;
        bgTargetLow = 0;
        bgTargetHigh = 0;
        bolusEstimate = 0.0;
        foodEstimate = 0.0;
        unabsorbedInsulinTotal = 0.0;
    }

    public double getCorrectionEstimate() { return correctionEstimate; }
    public long getBG() { return bloodGlucose; }
    public int getCarbohydrates() { return carbohydrates; }
    public double getICRatio() { return carbRatio; }
    public int getInsulinSensitivity() { return insulinSensitivity; }
    public int getBgTargetLow() { return bgTargetLow; }
    public int getBgTargetHigh() { return bgTargetHigh; }
    public double getBolusEstimate() { return bolusEstimate; }
    public double getFoodEstimate() { return foodEstimate; }
    public double getUnabsorbedInsulinTotal() { return unabsorbedInsulinTotal; }

    private double insulinDecode(int a, int b) {
        return ((a << 8) + b) / 40.0;
    }

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        if (PumpModel.isLargerFormat(model)) {
            length = 22;
        } else if (model.ordinal() >= PumpModel.MM523.ordinal()) {
            length = 20;
        }
        if (!simpleParse(length,data,2)) {
            return false;
        }
        if (PumpModel.isLargerFormat(model)) {
            carbohydrates = (asUINT8(data[8]) & 0x0c << 6) + asUINT8(data[7]);
            bloodGlucose = (asUINT8(data[8]) & 0x03 << 8) + asUINT8(data[1]);
            foodEstimate = insulinDecode(asUINT8(data[14]), asUINT8(data[15]));
            correctionEstimate = (double)((asUINT8(data[16])& 0b111000) << 5 + asUINT8(data[13]))/40.0;
            bolusEstimate = insulinDecode(asUINT8(data[19]), asUINT8(data[20]));
            unabsorbedInsulinTotal = insulinDecode(asUINT8(data[17]),asUINT8(data[18]));
            bgTargetLow = asUINT8(data[12]);
            bgTargetHigh = asUINT8(data[21]);
            insulinSensitivity = asUINT8(data[11]);
            carbRatio = (double)(((asUINT8(data[9]) & 0x07) << 8) + asUINT8(data[10]))/40.0;
        } else {
            carbohydrates = asUINT8(data[7]);
            bloodGlucose = ((asUINT8(data[8]) & 0x03) << 8) + asUINT8(data[1]);
            foodEstimate = (double)(asUINT8(data[13]))/10.0;
            correctionEstimate = (double)((asUINT8(data[14])<<8) + asUINT8(data[12])) / 10.0;
            bolusEstimate = (double)(asUINT8(data[18]))/10.0;
            unabsorbedInsulinTotal = (double)(asUINT8(data[16])) / 10.0;
            bgTargetLow = asUINT8(data[11]);
            bgTargetHigh = asUINT8(data[19]);
            insulinSensitivity = asUINT8(data[10]);
            carbRatio = (double)asUINT8(data[9]);
        }
        return true;
    }
}
