package com.kinwatt.powermeter.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.kinwatt.powermeter.R;
import com.kinwatt.powermeter.data.Position;
import com.kinwatt.powermeter.ui.widget.ChronometerView;
import com.kinwatt.powermeter.ui.widget.NumberView;

public class MapActivity extends ActivityBase implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PolylineOptions options;
    private Polyline line;

    private MapController controller;

    private Button buttonStart, buttonStop;

    private NumberView power;
    private NumberView speed;
    private ChronometerView duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        buttonStart = findViewById(R.id.button_start);
        buttonStop = findViewById(R.id.button_stop);

        buttonStop.setEnabled(false);

        duration = findViewById(R.id.duration);
        speed = findViewById(R.id.speed);
        power = findViewById(R.id.power);

        speed.setUnits("km/h");
        power.setUnits("W");

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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        controller = new MapController(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.stop();
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        LatLng malaga = new LatLng(36.7161622, -4.4233658);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(malaga));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        options = new PolylineOptions()
                .width(15)
                .color(Color.BLUE)
                .geodesic(true)
                .jointType(JointType.ROUND)
                .startCap(new RoundCap())
                .endCap(new RoundCap());
    }

    public void updateMap(Position position) {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(position.getLatitude(), position.getLongitude())));

            LatLng point = new LatLng(position.getLatitude(), position.getLongitude());

            if (!options.getPoints().contains(point)) {
                options.add(point);
            }

            Polyline newLine = mMap.addPolyline(options);
            if (line != null) {
                line.remove();
            }
            line = newLine;
        }
    }

    public void stopTimer() {
        duration.stop();
        buttonStop.setEnabled(false);
        buttonStart.setEnabled(true);
    }

    public void setSpeed(float speed) {
        this.speed.setValue(speed);
    }

    public void setPower(float power) {
        this.power.setValue(power);
    }

    public void clearMap() {
        if (mMap != null) {
            mMap.clear();
            options.getPoints().clear();
        }
    }
}
