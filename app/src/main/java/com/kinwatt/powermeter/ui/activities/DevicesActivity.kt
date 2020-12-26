package com.kinwatt.powermeter.ui.activities

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.ParcelUuid
import androidx.appcompat.app.AppCompatActivity
import android.view.View

import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.data.SensorData
import com.kinwatt.powermeter.data.ServiceData
import com.kinwatt.powermeter.data.provider.SensorProvider
import com.kinwatt.powermeter.databinding.ActivityDevicesBinding
import com.kinwatt.powermeter.ui.fragments.BluetoothDeviceFragment
import com.kinwatt.powermeter.ui.fragments.ConnectDeviceDialogFragment
import com.kinwatt.powermeter.ui.fragments.DeviceFragment

class DevicesActivity : AppCompatActivity(), BluetoothDeviceFragment.OnListFragmentInteractionListener {

    private lateinit var deviceFragment: DeviceFragment
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private lateinit var binding: ActivityDevicesBinding

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                if (bluetoothState == BluetoothAdapter.STATE_ON) {
                    val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
                    binding.connectButton.isEnabled = bluetoothLeScanner != null
                    unregisterReceiver(this)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDevicesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.connectButton.setOnClickListener {
            val dialogFragment = ConnectDeviceDialogFragment()
            dialogFragment.show(supportFragmentManager, "connect_devices_dialog")
        }

        deviceFragment = supportFragmentManager.findFragmentById(R.id.connected_devices) as DeviceFragment

        if (SensorProvider.getProvider(this).all.isEmpty()) {
            deviceFragment.view!!.visibility = View.GONE
        } else {
            binding.noDevices.visibility = View.GONE
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            registerReceiver(broadcastReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1)
        }

        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        binding.connectButton.isEnabled = bluetoothLeScanner != null
    }

    override fun onListFragmentInteraction(device: BluetoothDevice, uuids: List<ParcelUuid>) {
        val data = convert(device, uuids)
        if (!deviceFragment.contains(data)) {
            deviceFragment.add(data)

            deviceFragment.view!!.visibility = View.VISIBLE
            binding.noDevices.visibility = View.GONE
        }
    }

    private fun convert(device: BluetoothDevice, uuids: List<ParcelUuid>): SensorData {
        val sensor = SensorData()
        sensor.name = device.name
        sensor.address = device.address

        for (uuid in uuids) {
            sensor.services.add(ServiceData(uuid.uuid))
        }

        return sensor
    }
}
