package com.example.mystockwatch;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;

public class MyAxisFormatter implements IAxisValueFormatter {

    private long referenceTimestamp;
    private SimpleDateFormat dateFormat;
    private long divideBy;

    public MyAxisFormatter(SimpleDateFormat dateFormat,long ref, long divideBy){
        this.referenceTimestamp = ref;
        this.dateFormat = dateFormat;
        this.divideBy = divideBy;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        long date = referenceTimestamp + ((long)value*divideBy);
        return null;
    }
}
