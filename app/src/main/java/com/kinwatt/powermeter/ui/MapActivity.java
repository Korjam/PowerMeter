package com.kinwatt.powermeter.ui;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.kinwatt.powermeter.R;
import com.kinwatt.powermeter.data.Position;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private List<Position> positions = new ArrayList<>();

    private MapController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mMapFragment = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.map_container, mMapFragment).commit();
        mMapFragment.getMapAsync(this);

        controller = new MapController(this);
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng malaga = new LatLng(36.7161622, -4.4233658);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(malaga));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        /*
        Position firstPosition = record.getPositions().get(0);
        LatLng initial = new LatLng(firstPosition.getLatitude(), firstPosition.getLongitude());

        Position lastPosition = record.getLastPosition();
        LatLng end = new LatLng(lastPosition.getLatitude(), lastPosition.getLongitude());

        mMap.addMarker(new MarkerOptions().position(initial).title("Start"));
        mMap.addMarker(new MarkerOptions().position(end).title("End"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(initial));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        PolylineOptions options = new PolylineOptions().width(15).color(Color.BLUE).geodesic(true);
        for (Position position: record.getPositions()) {
            options.add(new LatLng(position.getLatitude(), position.getLongitude()));
        }

        mMap.addPolyline(options);
        */
    }

    public void updateMap(Position position) {
        // TODO: Mock of a Google Map update.
        positions.add(position);
        if (mMap != null) {
            mMap.clear();

            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(position.getLatitude(), position.getLongitude())));

            PolylineOptions options = new PolylineOptions().width(15).color(Color.BLUE).geodesic(true);
            for (Position p : positions) {
                options.add(new LatLng(p.getLatitude(), p.getLongitude()));
            }

            mMap.addPolyline(options);
        }
    }

    public void clearMap() {
        positions.clear();
        if (mMap != null) {
            mMap.clear();
        }
    }
}
