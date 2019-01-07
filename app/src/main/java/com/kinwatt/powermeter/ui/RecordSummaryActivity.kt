package com.kinwatt.powermeter.ui

import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.common.LocationUtils
import com.kinwatt.powermeter.data.Record
import com.kinwatt.powermeter.data.mappers.RecordMapper
import com.kinwatt.powermeter.ui.widget.NumberView
import com.kinwatt.powermeter.ui.widget.WorkaroundMapFragment
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

class RecordSummaryActivity : AppCompatActivity(), OnMapReadyCallback {

    private var record: Record? = null

    private var filename: String? = null

    private var mScrollView: ScrollView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_summary)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        mScrollView = findViewById(R.id.main_container)

        durationFormat.timeZone = TimeZone.getTimeZone("GTM")

        val duration = findViewById<TextView>(R.id.duration)
        val distance = findViewById<NumberView>(R.id.distance)
        distance.units = "km"
        val speed = findViewById<NumberView>(R.id.speed)
        speed.units = "km/h"

        filename = intent.getStringExtra("file_name")

        try {
            record = RecordMapper.load(filename)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (record == null || record!!.positions.lastOrNull() == null) {
            Toast.makeText(applicationContext, "Invalid file. Deleting record...", Toast.LENGTH_LONG).show()
            onDelete()
            return
        }

        supportActionBar!!.title = record!!.name

        LocationUtils.normalize(record!!.positions)
        var interpolated = LocationUtils.interpolate(record!!)

        distance.value = record!!.distance.toDouble() / 1000
        duration.text = durationFormat.format(Date(record!!.positions.lastOrNull()!!.timestamp))
        speed.value = interpolated.speed.toDouble() * 3.6

        val mMapFragment = supportFragmentManager.findFragmentById(R.id.map_container) as WorkaroundMapFragment
        mMapFragment.setListener { mScrollView!!.requestDisallowInterceptTouchEvent(true) }
        mMapFragment.getMapAsync(this)

        populateSpeedChart(record!!)
        populatePowerChart(interpolated)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
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
            onDelete();
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

    private fun onDelete() {
        //RecordProvider.getInstance().remove(record);
        onBackPressed()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val firstPosition = record!!.positions.first()
        val initial = LatLng(firstPosition.latitude, firstPosition.longitude)

        val lastPosition = record!!.positions.last()
        val end = LatLng(lastPosition.latitude, lastPosition.longitude)

        googleMap.addMarker(MarkerOptions().position(initial).title("Start"))
        googleMap.addMarker(MarkerOptions().position(end).title("End"))

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(initial))
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(15f))

        /*
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Position p = record.getPositions().get(0);
        builder.include(new LatLng(p.getLatitude(), p.getLongitude()));
        p = record.getLastPosition();
        builder.include(new LatLng(p.getLatitude(), p.getLongitude()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 16));
        */

        val options = PolylineOptions().width(15f).color(Color.BLUE).geodesic(true)
        for (position in record!!.positions) {
            options.add(LatLng(position.latitude, position.longitude))
        }

        googleMap.addPolyline(options)
    }

    private fun setSpeedValues(record: Record, lineChart: LineChart) {
        val speed = lineChart.data.getDataSetByIndex(0) as LineDataSet
        val altitude = lineChart.data.getDataSetByIndex(1) as LineDataSet

        for (position in record.positions) {
            speed.addEntry(Entry(position.timestamp.toFloat(), position.speed * 3.6f))
            altitude.addEntry(Entry(position.timestamp.toFloat(), max(position.altitude, 0.0).toFloat()))
        }

        lineChart.data.notifyDataChanged()
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }

    private fun updateBar(interpolated: Record, barChart: BarChart) {
        val range = 50
        val map = TreeMap<Int, Int>()

        var max = 0
        for (p in interpolated.positions) {
            var key = p.power.roundToInt() / range

            if (key != 0) {
                if (key > 1000 / range) {
                    key = 1000 / range
                }
                map[key] = if (map.containsKey(key)) map[key]!! + 1 else 1
            }
            max = max(max, key)
        }

        val powerSet = barChart.data.getDataSetByIndex(0) as BarDataSet

        for (i in (0..max).filter { map.contains(it) }) {
            powerSet.addEntry(BarEntry((i * range).toFloat(), map[i]!!.toFloat()))
        }

        barChart.data.notifyDataChanged()
        barChart.notifyDataSetChanged()
        barChart.invalidate()
    }

    private fun populateSpeedChart(record: Record) {
        var lineChart = findViewById<LineChart>(R.id.chart1)!!

        lineChart.setDrawGridBackground(false)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)

        val xAxis = lineChart.xAxis
        xAxis.setValueFormatter { value, _ -> durationFormat.format(Date(value.toLong())) }
        xAxis.enableGridDashedLine(10f, 10f, 0f)

        val speed = LineDataSet(ArrayList(), resources.getString(R.string.speed))
        speed.setDrawCircles(false)
        speed.color = Color.BLUE
        speed.axisDependency = YAxis.AxisDependency.LEFT
        speed.setDrawValues(false)
        speed.lineWidth = 1f
        speed.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)

        val altitude = LineDataSet(ArrayList(), resources.getString(R.string.altitude))
        altitude.setDrawCircles(false)
        altitude.color = Color.GREEN
        altitude.axisDependency = YAxis.AxisDependency.RIGHT
        altitude.setDrawValues(false)
        altitude.lineWidth = 1f
        altitude.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
        altitude.setDrawFilled(true)

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(speed)
        dataSets.add(altitude)

        val line = LineData(dataSets)

        lineChart.data = line

        setSpeedValues(record, lineChart)
    }

    private fun populatePowerChart(interpolated: Record) {
        val barChart = findViewById<BarChart>(R.id.chart2)!!

        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)

        barChart.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        //barChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        barChart.setPinchZoom(false)

        barChart.setDrawGridBackground(false)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.labelCount = 7

        val leftAxis = barChart.axisLeft
        leftAxis.setLabelCount(8, false)
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 15f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val rightAxis = barChart.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.setLabelCount(8, false)
        rightAxis.spaceTop = 15f
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val l = barChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.form = Legend.LegendForm.SQUARE
        l.formSize = 9f
        l.textSize = 11f
        l.xEntrySpace = 4f

        val powerSet = BarDataSet(ArrayList(), resources.getString(R.string.instant_power))
        powerSet.setDrawIcons(false)
        powerSet.color = resources.getColor(R.color.colorPrimary, theme)

        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(powerSet)

        val data = BarData(dataSets)
        data.setValueTextSize(0f)
        data.barWidth = 45f

        barChart.data = data

        updateBar(interpolated, barChart)
    }

    companion object {

        private val durationFormat = SimpleDateFormat("H:mm:ss")
    }
}
