package com.kinwatt.powermeter.ui.fragments.steps;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinwatt.powermeter.R;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

public class QuestionCheckboxFragment extends Fragment implements Step {
    private static final String ARG_QUESTION = "question";
    private static final String ARG_PARAM1 = "values";

    private int questionId;
    private int[] checkBoxIds;

    public QuestionCheckboxFragment() {
    }

    public static QuestionCheckboxFragment newInstance(@StringRes int question, @StringRes int... values) {
        QuestionCheckboxFragment fragment = new QuestionCheckboxFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_QUESTION, question);
        args.putIntArray(ARG_PARAM1, values);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            questionId = getArguments().getInt(ARG_QUESTION);
            checkBoxIds = getArguments().getIntArray(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question_checkbox, container, false);

        TextView text = view.findViewById(R.id.question);
        text.setText(questionId);

        LinearLayout layout = view.findViewById(R.id.container);

        for (int id : checkBoxIds) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setId(id);
            checkBox.setText(id);
            layout.addView(checkBox);
        }

        return view;
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}
