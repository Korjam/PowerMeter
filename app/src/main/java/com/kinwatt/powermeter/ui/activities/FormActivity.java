package com.kinwatt.powermeter.ui.activities;

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
        int accentColor = getResources().getColor(R.color.colorAccent);
        mStepperLayout.setBackButtonColor(accentColor);
        mStepperLayout.setNextButtonColor(accentColor);
        mStepperLayout.setCompleteButtonColor(accentColor);
    }

    public static class QuestionStepperAdapter extends AbstractFragmentStepAdapter {

        public QuestionStepperAdapter(FragmentManager fm, Context context) {
            super(fm, context);
        }

        @Override
        public Step createStep(int position) {

            switch (position) {
                case 0:
                    return QuestionNumberFragment.newInstance(R.string.question_kilometers);
                case 1:
                    return QuestionCheckboxFragment.newInstance(R.string.question_cycling,
                            R.string.mountain,
                            R.string.road,
                            R.string.roller,
                            R.string.velodrome,
                            R.string.spinning);
                case 2:
                    return new QuestionPowerMeterFragment();
                case 3:
                    return QuestionCheckboxFragment.newInstance(R.string.question_features,
                            R.string.q_roller,
                            R.string.q_aerodynamics,
                            R.string.q_strava,
                            R.string.q_videogames);
                case 4:
                    return QuestionTextFragment.newInstance(R.string.question_suggestions);
            }

            throw new RuntimeException();
        }

        @Override
        public int getCount() {
            return 5;
        }
    }
}
