package com.kinwatt.powermeter.ui.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View

import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.data.Bike
import com.kinwatt.powermeter.data.BikeType
import com.kinwatt.powermeter.data.User
import com.kinwatt.powermeter.data.mappers.UserMapper

import java.io.File
import java.io.IOException

import kotlinx.android.synthetic.main.activity_user_edit.*

class UserEditActivity : AppCompatActivity() {

    private var user = User()

    private lateinit var userFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_edit)

        radio_mountain.tag = BikeType.Mountain
        radio_road.tag = BikeType.Road

        userFile = File(filesDir, "user_data.json")

        if (userFile.exists()) {
            goToMain()
        }

        continueButton.setOnClickListener {
            if (validateData()) {
                try {
                    UserMapper.save(user, userFile)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                goToMain()
            }
        }
    }

    private fun goToMain() {
        val intent = Intent(this@UserEditActivity, RecordListActivity::class.java)
        startActivity(intent)
    }

    private fun validateData(): Boolean {
        var res = true

        val name = nameView.text.toString()
        val age = ageView.text.toString()
        val weight = weightView.text.toString()
        val height = heightView.text.toString()
        val bikeWeight = bikeWeightView.text.toString()

        var focusView: View? = null

        if (TextUtils.isEmpty(bikeWeight)) {
            bikeWeightView.error = getString(R.string.error_field_required)
            res = false
            focusView = bikeWeightView
        }

        if (bikeTypeView.checkedRadioButtonId == 0) {
            radio_mountain.error = getString(R.string.error_field_required)
            focusView = bikeTypeView
            res = false
        } else {
            radio_mountain.error = null
        }

        if (TextUtils.isEmpty(height)) {
            heightView.error = getString(R.string.error_field_required)
            res = false
            focusView = heightView
        }

        if (TextUtils.isEmpty(weight)) {
            weightView.error = getString(R.string.error_field_required)
            res = false
            focusView = weightView
        }

        if (TextUtils.isEmpty(age)) {
            ageView.error = getString(R.string.error_field_required)
            res = false
            focusView = ageView
        }

        if (TextUtils.isEmpty(name)) {
            nameView.error = getString(R.string.error_field_required)
            res = false
            focusView = nameView
        } else if (!isValidName(name)) {
            nameView.error = getString(R.string.error_invalid_name)
            res = false
            focusView = nameView
        }

        if (!res) {
            focusView!!.requestFocus()
        } else {
            user.name = name
            user.age = Integer.parseInt(age)
            user.height = Integer.parseInt(height)
            user.weight = java.lang.Float.parseFloat(weight)

            val bike = Bike()
            bike.type = findViewById<View>(bikeTypeView!!.checkedRadioButtonId).tag as BikeType
            bike.weight = java.lang.Float.parseFloat(bikeWeight)

            user.bikes.add(bike)
        }

        return res
    }

    private fun isValidName(name: String): Boolean {
        for (c in name.toCharArray()) {
            if (!Character.isLetter(c)) {
                return false
            }
        }

        return true
    }
}
