package com.kinwatt.powermeter.ui.activities

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AppCompatActivity

import com.kinwatt.powermeter.R
import com.kinwatt.powermeter.databinding.ActivityFormBinding
import com.kinwatt.powermeter.ui.fragments.steps.QuestionCheckboxFragment
import com.kinwatt.powermeter.ui.fragments.steps.QuestionPowerMeterFragment
import com.kinwatt.powermeter.ui.fragments.steps.QuestionNumberFragment
import com.kinwatt.powermeter.ui.fragments.steps.QuestionTextFragment
import com.stepstone.stepper.Step
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter

class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFormBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.stepperLayout.adapter = QuestionStepperAdapter(supportFragmentManager, this)

        val accentColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            resources.getColor(R.color.colorAccent, theme)
        else
            resources.getColor(R.color.colorAccent)

        binding.stepperLayout!!.setBackButtonColor(accentColor)
        binding.stepperLayout!!.setNextButtonColor(accentColor)
        binding.stepperLayout!!.setCompleteButtonColor(accentColor)
    }

    class QuestionStepperAdapter(fm: FragmentManager, context: Context) : AbstractFragmentStepAdapter(fm, context) {

        override fun createStep(position: Int): Step {

            when (position) {
                0 -> return QuestionNumberFragment.newInstance(R.string.question_kilometers)
                1 -> return QuestionCheckboxFragment.newInstance(R.string.question_cycling,
                        R.string.mountain,
                        R.string.road,
                        R.string.roller,
                        R.string.velodrome,
                        R.string.spinning)
                2 -> return QuestionPowerMeterFragment()
                3 -> return QuestionCheckboxFragment.newInstance(R.string.question_features,
                        R.string.q_roller,
                        R.string.q_aerodynamics,
                        R.string.q_strava,
                        R.string.q_videogames)
                4 -> return QuestionTextFragment.newInstance(R.string.question_suggestions)
            }

            throw RuntimeException()
        }

        override fun getCount(): Int {
            return 5
        }
    }
}
