package com.kinwatt.powermeter.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.kinwatt.powermeter.R;
import com.kinwatt.powermeter.common.LocationUtils;
import com.kinwatt.powermeter.data.Position;
import com.kinwatt.powermeter.data.Record;
import com.kinwatt.powermeter.data.mappers.RecordMapper;
import com.kinwatt.powermeter.ui.widget.NumberView;
import com.kinwatt.powermeter.ui.widget.WorkaroundMapFragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class RecordSummaryActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final SimpleDateFormat durationFormat = new SimpleDateFormat("H:mm:ss");

    private GoogleMap mMap;
    private Record record, interpolated;

    private LineChart lineChart;
    private BarChart barChart;

    private String filename;

    private ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_summary);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mScrollView = findViewById(R.id.main_container);

        durationFormat.setTimeZone(TimeZone.getTimeZone("GTM"));

        TextView duration = findViewById(R.id.duration);
        NumberView distance = findViewById(R.id.distance);
        distance.setUnits("km");
        NumberView speed = findViewById(R.id.speed);
        speed.setUnits("km/h");

        Intent intent = getIntent();
        filename = intent.getStringExtra("file_name");

        try {
            record = RecordMapper.load(filename);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (record == null || record.getLastPosition() == null) {
            Toast.makeText(getApplicationContext(), "Invalid file. Deleting record...", Toast.LENGTH_LONG).show();
            OnDelete();
            return;
        }

        getSupportActionBar().setTitle(record.getName());

        LocationUtils.normalize(record.getPositions());
        interpolated = LocationUtils.interpolate(record);

        distance.setValue(record.getDistance() / 1000);
        duration.setText(durationFormat.format(new Date(record.getLastPosition().getTimestamp())));
        speed.setValue(interpolated.getSpeed() * 3.6f);

        WorkaroundMapFragment mMapFragment = (WorkaroundMapFragment)getSupportFragmentManager().findFragmentById(R.id.map_container);
        mMapFragment.setListener(() -> mScrollView.requestDisallowInterceptTouchEvent(true));
        mMapFragment.getMapAsync(this);

        populateSpeedChart();
        populatePowerChart();

        interpolated = null;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete) {
            OnDelete();
            return true;
        }
        else if (id == R.id.share) {
            Intent share = new Intent(Intent.ACTION_SEND);

            share.setType("application/pdf");
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filename)));
            //share.setPackage("com.whatsapp");

            //share.putExtra(Intent.EXTRA_SUBJECT, "Sharing File...");
            //share.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

            startActivity(Intent.createChooser(share, "Share File"));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */

    private void OnDelete() {
        //RecordProvider.getInstance().remove(record);
        onBackPressed();
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Position firstPosition = record.getPositions().get(0);
        LatLng initial = new LatLng(firstPosition.getLatitude(), firstPosition.getLongitude());

        Position lastPosition = record.getLastPosition();
        LatLng end = new LatLng(lastPosition.getLatitude(), lastPosition.getLongitude());

        mMap.addMarker(new MarkerOptions().position(initial).title("Start"));
        mMap.addMarker(new MarkerOptions().position(end).title("End"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(initial));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        /*
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Position p = record.getPositions().get(0);
        builder.include(new LatLng(p.getLatitude(), p.getLongitude()));
        p = record.getLastPosition();
        builder.include(new LatLng(p.getLatitude(), p.getLongitude()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 16));
        */

        PolylineOptions options = new PolylineOptions().width(15).color(Color.BLUE).geodesic(true);
        for (Position position : record.getPositions()) {
            options.add(new LatLng(position.getLatitude(), position.getLongitude()));
        }

        mMap.addPolyline(options);
    }

    private void setSpeedValues() {
        LineDataSet speed = (LineDataSet)lineChart.getData().getDataSetByIndex(0);
        LineDataSet altitude = (LineDataSet)lineChart.getData().getDataSetByIndex(1);

        for (Position position : record.getPositions()) {
            speed.addEntry(new Entry(position.getTimestamp(), position.getSpeed() * 3.6f));
            altitude.addEntry(new Entry(position.getTimestamp(), Math.max(position.getAltitude(), 0)));
        }

        lineChart.getData().notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    private void updateBar() {
        final int range = 50;
        Map<Integer, Integer> map = new TreeMap<>();

        int max = 0;
        for (Position p : interpolated.getPositions()) {
            int key = Math.round(p.getPower()) / range;

            if (key != 0) {
                if (key > (1000 / range)) {
                    key = 1000 / range;
                }

                if (map.containsKey(key)) {
                    map.put(key, map.get(key) + 1);
                }
                else {
                    map.put(key, 1);
                }
            }
            max = Math.max(max, key);
        }

        BarDataSet powerSet = (BarDataSet)barChart.getData().getDataSetByIndex(0);

        for (int i = 0; i <= max; i++) {
            if (map.containsKey(i)) {
                powerSet.addEntry(new BarEntry(i * range, map.get(i)));
            }
        }

        barChart.getData().notifyDataChanged();
        barChart.notifyDataSetChanged();
        barChart.invalidate();
    }

    private void populateSpeedChart() {
        lineChart = findViewById(R.id.chart1);

        lineChart.setDrawGridBackground(false);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter((value, axis) -> durationFormat.format(new Date((long) value)));
        xAxis.enableGridDashedLine(10f, 10f, 0f);

        LineDataSet speed, altitude;

        speed = new LineDataSet(new ArrayList<>(), getResources().getString(R.string.speed));
        speed.setDrawCircles(false);
        speed.setColor(Color.BLUE);
        speed.setAxisDependency(YAxis.AxisDependency.LEFT);
        speed.setDrawValues(false);
        speed.setLineWidth(1f);
        speed.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));

        altitude = new LineDataSet(new ArrayList<>(), getResources().getString(R.string.altitude));
        altitude.setDrawCircles(false);
        altitude.setColor(Color.GREEN);
        altitude.setAxisDependency(YAxis.AxisDependency.RIGHT);
        altitude.setDrawValues(false);
        altitude.setLineWidth(1f);
        altitude.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        altitude.setDrawFilled(true);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(speed);
        dataSets.add(altitude);

        LineData line = new LineData(dataSets);

        lineChart.setData(line);

        setSpeedValues();
    }

    private void populatePowerChart() {
        barChart = findViewById(R.id.chart2);

        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);

        barChart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        //barChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        barChart.setPinchZoom(false);

        barChart.setDrawGridBackground(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        Legend l = barChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        BarDataSet powerSet = new BarDataSet(new ArrayList<>(), getResources().getString(R.string.instant_power));
        powerSet.setDrawIcons(false);
        powerSet.setColor(getResources().getColor(R.color.colorPrimary));

        List<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(powerSet);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(0);
        data.setBarWidth(45);

        barChart.setData(data);

        updateBar();
    }
}
