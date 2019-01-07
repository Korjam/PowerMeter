package com.kinwatt.powermeter.ui

import android.content.Context
import android.content.Intent
import android.location.Location
import android.support.v7.app.AlertDialog
import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.common.AppSettings
import com.kinwatt.powermeter.data.Record
import com.kinwatt.powermeter.data.User
import com.kinwatt.powermeter.data.mappers.UserMapper
import com.kinwatt.powermeter.data.provider.RecordProvider
import com.kinwatt.powermeter.model.CyclingOutdoorPowerAlgorithm
import com.kinwatt.powermeter.model.PowerListener
import com.kinwatt.powermeter.model.PowerProvider
import com.kinwatt.powermeter.sensor.LocationListener
import com.kinwatt.powermeter.sensor.LocationProvider
import java.io.File
import java.io.IOException
import java.util.*

class ActivityOutdoorController(private val context: Context, private val view: ActivityView) : LocationListener, PowerListener {
    private val settings: AppSettings
    private val dialogBuilder: AlertDialog.Builder

    private val locationProvider: LocationProvider
    private var lastLocation: Location? = null
    private var distance = 0f

    private val powerProvider: PowerProvider
    private val powers = ArrayDeque<Float>(10)

    private val recordProvider: RecordProvider
    private var record: Record? = null

    private var user: User? = null

    private var running = false

    init {

        settings = AppSettings.getAppSettings(context)

        recordProvider = RecordProvider.getProvider(context)

        locationProvider = LocationProvider.createProvider(context, LocationProvider.GPS_PROVIDER)
        /*
        // DEBUG PURPOSES
        locationProvider = LocationProvider.createProvider(context, LocationProvider.MOCK_PROVIDER);
        try {
            File file = new File(Environment.getExternalStorageDirectory(), "Cycling outdoor_20180312_085247.json");
            ((LocationProviderMock)locationProvider).setRecord(RecordMapper.load(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        try {
            user = UserMapper.load(File(context.filesDir, "user_data.json"))
        } catch (e: IOException) {
            user = null
            e.printStackTrace()
        }

        powerProvider = PowerProvider(CyclingOutdoorPowerAlgorithm(user), locationProvider)

        /*
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        */

        locationProvider.addListener(this)
        powerProvider.addListener(this)

        //Setup dialog
        dialogBuilder = AlertDialog.Builder(this.context)
                .setTitle(R.string.feedback_title)
                .setMessage(R.string.feedback_message)
                .setPositiveButton(R.string.yes) { _, _ -> this.context.startActivity(Intent(this.context, FormActivity::class.java)) }
                .setNegativeButton(R.string.no) { _, _ -> settings.isQuestionaryCompleted = true }
                .setNeutralButton(R.string.remind_later) { _, _ -> }
    }

    fun start() {
        if (!running) {
            running = true
            locationProvider.start()
            powerProvider.reset()
            record = Record()
            record!!.name = "Cycling outdoor"
            record!!.date = Date()
        }
    }

    fun stop() {
        if (running) {
            running = false
            locationProvider.stop()
            powerProvider.reset()

            recordProvider.add(record)

            /*
            if (!settings.isQuestionaryCompleted()) {
                dialogBuilder.create().show();
            }
            */
        }
    }

    override fun onLocationChanged(location: Location) {
        record!!.addPosition(location)

        view.setSpeed(location.speed)
        view.setAltitude(location.altitude)

        if (lastLocation != null) {
            distance += lastLocation!!.distanceTo(location)
            view.setDistance(distance)
        }

        lastLocation = location
    }

    override fun onPowerMeasured(eventTime: Long, power: Float) {
        powers.add(power)
        if (powers.size >= 3) {
            val reversed = powers.reversed()

            view.setPowerAverage3(reversed.asSequence().take(3).average().toFloat())

            if (powers.size >= 5) {
                view.setPowerAverage5(reversed.asSequence().take(5).average().toFloat())

                if (powers.size >= 10) {
                    view.setPowerAverage10(reversed.asSequence().take(10).average().toFloat())
                }
            }
        }
    }
}
