package com.kinwatt.powermeter.ui.fragments

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import androidx.fragment.app.DialogFragment
import android.util.Log
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.databinding.FragmentConnectDeviceBinding
import com.kinwatt.powermeter.sensor.bluetooth.SpeedAndCadenceClient

import java.util.ArrayList
import java.util.UUID

class ConnectDeviceDialogFragment : DialogFragment() {

    private var fragment: BluetoothDeviceFragment? = null

    private lateinit var binding: FragmentConnectDeviceBinding
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var handler: Handler

    private var searching = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device

            if (searching && !fragment!!.contains(device)) {
                Log.i(TAG, String.format("BluetoothDevice founded: %s %s.", device.address, device.name))
                fragment!!.add(device, result.scanRecord!!.serviceUuids)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler = Handler()

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        if (!bluetoothAdapter.isEnabled) {
            bluetoothAdapter.enable()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (viewInstance != null) {
            (viewInstance!!.parent as ViewGroup)?.removeView(viewInstance)
        }
        try {
            binding = FragmentConnectDeviceBinding.inflate(inflater, container, false)
            viewInstance = binding.root
        } catch (e: InflateException) {
            // View already created
        }

        return viewInstance
        //return inflater.inflate(R.layout.fragment_connect_device, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startStopButton.setOnClickListener {
            if (searching) {
                stopScan()
            } else {
                startScan()
            }
        }
        binding.cancelButton.setOnClickListener { dismiss() }

        binding.progressBar.visibility = View.GONE

        fragment = fragmentManager!!.findFragmentById(R.id.search_results) as BluetoothDeviceFragment
        startScan()
    }

    @JvmOverloads
    fun startScan(serviceId: UUID = SpeedAndCadenceClient.SERVICE_UUID) {
        startScan(*arrayOf(serviceId))
    }

    fun startScan(vararg serviceIds: UUID) {
        val ids = ArrayList<UUID>()
        for (id in serviceIds) {
            ids.add(id)
        }
        startScan(ids)
    }

    fun startScan(serviceIds: Iterable<UUID>) {
        if (!searching) {
            val filters = ArrayList<ScanFilter>()

            val builder = ScanFilter.Builder()
            for (uuid in serviceIds) {
                builder.setServiceUuid(ParcelUuid(uuid))
                filters.add(builder.build())
            }

            bluetoothLeScanner.startScan(filters, ScanSettings.Builder().build(), scanCallback)

            Log.i(TAG, "LE Search Started.")

            searching = true

            binding.progressBar.visibility = View.VISIBLE

            binding.startStopButton.setText(R.string.stop)

            binding.searchTitle.setText(R.string.searching_devices)

            handler.postDelayed({
                if (searching) {
                    stopScan()
                }
            }, 15000)
        }
    }

    fun stopScan() {
        if (searching) {
            bluetoothLeScanner.stopScan(scanCallback)
            Log.i(TAG, "LE Search Stopped.")
            searching = false

            binding.progressBar.visibility = View.GONE

            binding.startStopButton.setText(R.string.scan)

            binding.searchTitle.setText(R.string.search_results)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.stopScan()
    }

    companion object {

        private val TAG = "BluetoothSearch"

        private var viewInstance: View? = null
    }
}
