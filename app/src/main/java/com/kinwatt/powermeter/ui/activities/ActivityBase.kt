package com.kinwatt.powermeter.ui.activities

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import com.kinwatt.powermeter.R

open class ActivityBase : AppCompatActivity() {

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.main, menu)
        val devicesItem = menu.findItem(R.id.devices_setting)
        devicesItem.isVisible = BluetoothAdapter.getDefaultAdapter() != null
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.devices_setting) {
            val intent = Intent(this, DevicesActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
