package com.kinwatt.powermeter.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.kinwatt.powermeter.R;
import com.kinwatt.powermeter.ui.fragments.steps.QuestionCheckboxFragment;
import com.kinwatt.powermeter.ui.fragments.steps.QuestionPowerMeterFragment;
import com.kinwatt.powermeter.ui.fragments.steps.QuestionNumberFragment;
import com.kinwatt.powermeter.ui.fragments.steps.QuestionTextFragment;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;

public class FormActivity extends AppCompatActivity {

    private StepperLayout mStepperLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        mStepperLayout = findViewById(R.id.stepperLayout);
        mStepperLayout.setAdapter(new QuestionStepperAdapter(getSupportFragmentManager(), this));
    }

    public static class QuestionStepperAdapter extends AbstractFragmentStepAdapter {

        public QuestionStepperAdapter(FragmentManager fm, Context context) {
            super(fm, context);
        }

        @Override
        public Step createStep(int position) {

            switch (position) {
                case 0:
                    //return QuestionTextFragment.newInstance(R.string.question_suggestions);
                    return QuestionNumberFragment.newInstance(R.string.question_kilometers);
                case 1:
                    return getStepBikeType();
                case 2:
                    return new QuestionPowerMeterFragment();
                case 3:
                    return getStep();
                case 4:
                    return QuestionTextFragment.newInstance(R.string.question_suggestions);
            }

            throw new RuntimeException();
        }

        private Step getStepBikeType() {
            int[] arr = new int[5];
            arr[0] = R.string.mountain;
            arr[1] = R.string.road;
            arr[2] = R.string.roller;
            arr[3] = R.string.velodrome;
            arr[4] = R.string.spinning;
            return QuestionCheckboxFragment.newInstance(R.string.question_cycling, arr);
        }

        private Step getStep() {
            int[] arr = new int[4];
            arr[0] = R.string.q_roller;
            arr[1] = R.string.q_aerodynamics;
            arr[2] = R.string.q_strava;
            arr[3] = R.string.q_videogames;
            return QuestionCheckboxFragment.newInstance(R.string.question_kilometers, arr);
        }

        @Override
        public int getCount() {
            return 5;
        }
    }
}
