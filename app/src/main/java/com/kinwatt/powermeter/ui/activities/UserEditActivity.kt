package com.kinwatt.powermeter.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View

import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.data.Bike
import com.kinwatt.powermeter.data.BikeType
import com.kinwatt.powermeter.data.User
import com.kinwatt.powermeter.data.mappers.UserMapper
import com.kinwatt.powermeter.databinding.ActivityUserEditBinding

import java.io.File
import java.io.IOException

class UserEditActivity : AppCompatActivity() {

    private var user = User()

    private lateinit var binding: ActivityUserEditBinding
    private lateinit var userFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserEditBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.radioMountain.tag = BikeType.Mountain
        binding.radioRoad.tag = BikeType.Road

        userFile = File(filesDir, "user_data.json")

        if (userFile.exists()) {
            goToMain()
        }

        binding.continueButton.setOnClickListener {
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

        val name = binding.nameView.text.toString()
        val age = binding.ageView.text.toString()
        val weight = binding.weightView.text.toString()
        val height = binding.heightView.text.toString()
        val bikeWeight = binding.bikeWeightView.text.toString()

        var focusView: View? = null

        if (TextUtils.isEmpty(bikeWeight)) {
            binding.bikeWeightView.error = getString(R.string.error_field_required)
            res = false
            focusView = binding.bikeWeightView
        }

        if (binding.bikeTypeView.checkedRadioButtonId == 0) {
            binding.radioMountain.error = getString(R.string.error_field_required)
            focusView = binding.bikeTypeView
            res = false
        } else {
            binding.radioMountain.error = null
        }

        if (TextUtils.isEmpty(height)) {
            binding.heightView.error = getString(R.string.error_field_required)
            res = false
            focusView = binding.heightView
        }

        if (TextUtils.isEmpty(weight)) {
            binding.weightView.error = getString(R.string.error_field_required)
            res = false
            focusView = binding.weightView
        }

        if (TextUtils.isEmpty(age)) {
            binding.ageView.error = getString(R.string.error_field_required)
            res = false
            focusView = binding.ageView
        }

        if (TextUtils.isEmpty(name)) {
            binding.nameView.error = getString(R.string.error_field_required)
            res = false
            focusView = binding.nameView
        } else if (!isValidName(name)) {
            binding.nameView.error = getString(R.string.error_invalid_name)
            res = false
            focusView = binding.nameView
        }

        if (!res) {
            focusView!!.requestFocus()
        } else {
            user.name = name
            user.age = Integer.parseInt(age)
            user.height = Integer.parseInt(height)
            user.weight = java.lang.Float.parseFloat(weight)

            val bike = Bike()
            bike.type = findViewById<View>(binding.bikeTypeView!!.checkedRadioButtonId).tag as BikeType
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
