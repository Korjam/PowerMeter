package com.kinwatt.powermeter.ui.widget;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressWarnings("all")
public class NumberView extends TextView {

    private double value = 0;

    private String units;

    public NumberView(Context context) {
        super(context);
    }

    public NumberView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NumberView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public String getUnits() {
        return units;
    }
    public void setUnits(String units) {
        this.units = units;
    }

    public double getValue() {
        return value;
    }
    public void setValue(double value) {
        this.value = value;
        if (units == null) {
            setText(value != 0 ? String.format("%.2f", value) : "--");
        }
        else {
            setText(value != 0 ? String.format("%.2f %s", value, units) : "-- " + units);
        }
    }
    /*
    public void setValue(long value) {
        this.value = value;
        if (units == null) {
            setText(value != 0 ? String.valueOf(value) : "--");
        }
        else {
            setText(value != 0 ? String.format("%s %s", value, units) : "-- " + units);
        }
    }
    */
}
