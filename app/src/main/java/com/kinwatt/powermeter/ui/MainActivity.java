package com.kinwatt.powermeter.ui;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

import com.kinwatt.powermeter.R;
import com.kinwatt.powermeter.ui.widget.ChronometerView;
import com.kinwatt.powermeter.ui.widget.NumberView;

public class MainActivity extends ActivityBase implements ActivityView {

    private Button buttonStart, buttonStop;

    private NumberView power3, power5, power10;
    private NumberView speed, altitude, distance;
    private ChronometerView duration;

    private ActivityController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        buttonStart = findViewById(R.id.button_start);
        buttonStop = findViewById(R.id.button_stop);

        buttonStop.setEnabled(false);

        duration = findViewById(R.id.duration);
        speed = findViewById(R.id.speed);
        altitude = findViewById(R.id.altitude);
        distance = findViewById(R.id.distance);

        power3 = findViewById(R.id.power_3s);
        power5 = findViewById(R.id.power_5s);
        power10 = findViewById(R.id.power_10s);

        buttonStart.setOnClickListener(v -> {
            buttonStart.setEnabled(false);

            duration.restart();
            controller.start();

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            buttonStop.setEnabled(true);
        });
        buttonStop.setOnClickListener(v -> {
            buttonStop.setEnabled(false);

            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            duration.stop();
            controller.stop();

            buttonStart.setEnabled(true);
        });

        controller = new ActivityController(this, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.stop();
    }

    @Override
    public void setSpeed(float speed) {
        this.speed.setValue(speed * 3.6f);
    }

    @Override
    public void setAltitude(double altitude) {
        this.altitude.setValue(altitude);
    }

    @Override
    public void setDistance(float distance) {
        this.distance.setValue(distance);
    }

    @Override
    public void setPowerAverage3(float power) {
        this.power3.setValue(power);
    }

    @Override
    public void setPowerAverage5(float power) {
        this.power5.setValue(power);
    }

    @Override
    public void setPowerAverage10(float power) {
        this.power10.setValue(power);
    }
}
