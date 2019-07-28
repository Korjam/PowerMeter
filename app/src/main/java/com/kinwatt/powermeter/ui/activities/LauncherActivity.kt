package com.kinwatt.powermeter.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity

import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.data.provider.RecordProvider
import com.kinwatt.powermeter.data.provider.SensorProvider

import java.util.ArrayList

class LauncherActivity : AppCompatActivity() {

    private val permissionsNeeded = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        // Bootstrap
        SensorProvider.getProvider(this)
        RecordProvider.getProvider(this)

        permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)

        if (requestPermissions()) {
            Handler().postDelayed({ goToUserEdit() }, 1500)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestPermissions()) {
            goToUserEdit()
        }
    }

    private fun requestPermissions(): Boolean {
        for (permission in permissionsNeeded) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 0)
                return false
            }
        }
        return true
    }

    private fun goToUserEdit() {
        val intent = Intent(this, UserEditActivity::class.java)
        startActivity(intent)
    }
}
