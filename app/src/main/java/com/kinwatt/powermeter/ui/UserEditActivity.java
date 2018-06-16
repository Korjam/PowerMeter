package com.kinwatt.powermeter.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.kinwatt.powermeter.R;
import com.kinwatt.powermeter.data.Bike;
import com.kinwatt.powermeter.data.BikeType;
import com.kinwatt.powermeter.data.User;
import com.kinwatt.powermeter.data.mappers.UserMapper;

import java.io.File;
import java.io.IOException;

public class UserEditActivity extends AppCompatActivity {

    private EditText nameView, ageView, weightView, heightView, bikeWeightView;
    private RadioGroup bikeTypeView;
    private Button continueButton;

    private User user;

    private File userFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);

        nameView = findViewById(R.id.text_name);
        ageView = findViewById(R.id.text_age);
        weightView = findViewById(R.id.text_weight);
        heightView = findViewById(R.id.text_height);
        bikeWeightView = findViewById(R.id.text_bike_weight);
        bikeTypeView = findViewById(R.id.radio_group_bike);
        continueButton = findViewById(R.id.button_continue);

        findViewById(R.id.radio_mountain).setTag(BikeType.Mountain);
        findViewById(R.id.radio_road).setTag(BikeType.Road);

        userFile = new File(getFilesDir(), "user_data.json");

        if (userFile.exists()) {
            goToMain();
        }

        continueButton.setOnClickListener(v -> {
            if (validateData()) {
                try {
                    UserMapper.save(user, userFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                goToMain();
            }
        });

        user = new User();
    }

    private void goToMain() {
        Intent intent = new Intent(UserEditActivity.this, RecordListActivity.class);
        startActivity(intent);
    }

    private boolean validateData() {
        boolean res = true;

        String name = nameView.getText().toString();
        String age = ageView.getText().toString();
        String weight = weightView.getText().toString();
        String height = heightView.getText().toString();
        String bikeWeight = bikeWeightView.getText().toString();

        View focusView = null;

        if (TextUtils.isEmpty(bikeWeight)) {
            bikeWeightView.setError(getString(R.string.error_field_required));
            res = false;
            focusView = bikeWeightView;
        }

        if (bikeTypeView.getCheckedRadioButtonId() == 0) {
            RadioButton but =  findViewById(R.id.radio_mountain);
            but.setError(getString(R.string.error_field_required));
            focusView = bikeTypeView;
            res = false;
        }
        else {
            RadioButton but =  findViewById(R.id.radio_mountain);
            but.setError(null);
        }

        if (TextUtils.isEmpty(height)) {
            heightView.setError(getString(R.string.error_field_required));
            res = false;
            focusView = heightView;
        }

        if (TextUtils.isEmpty(weight)) {
            weightView.setError(getString(R.string.error_field_required));
            res = false;
            focusView = weightView;
        }

        if (TextUtils.isEmpty(age)) {
            ageView.setError(getString(R.string.error_field_required));
            res = false;
            focusView = ageView;
        }

        if (TextUtils.isEmpty(name)) {
            nameView.setError(getString(R.string.error_field_required));
            res = false;
            focusView = nameView;
        }
        else if (!isValidName(name)) {
            nameView.setError(getString(R.string.error_invalid_name));
            res = false;
            focusView = nameView;
        }

        if (!res) {
            focusView.requestFocus();
        }
        else {
            user.setName(name);
            user.setAge(Integer.parseInt(age));
            user.setHeight(Integer.parseInt(height));
            user.setWeight(Float.parseFloat(weight));

            Bike bike = new Bike();
            bike.setType((BikeType) findViewById(bikeTypeView.getCheckedRadioButtonId()).getTag());
            bike.setWeight(Float.parseFloat(bikeWeight));

            user.getBikes().add(bike);
        }

        return res;
    }

    private static boolean isValidName(String name) {
        for (char c : name.toCharArray()) {
            if(!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }
}
