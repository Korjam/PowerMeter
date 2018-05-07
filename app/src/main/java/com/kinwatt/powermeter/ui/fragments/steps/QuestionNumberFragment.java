package com.kinwatt.powermeter.ui.fragments.steps;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kinwatt.powermeter.R;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

public class QuestionNumberFragment extends Fragment implements Step {
    private static final String ARG_QUESTION = "question";

    private int questionId;

    public QuestionNumberFragment() {
    }

    public static QuestionNumberFragment newInstance(@StringRes int question) {
        QuestionNumberFragment fragment = new QuestionNumberFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_QUESTION, question);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            questionId = getArguments().getInt(ARG_QUESTION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question_number, container, false);

        TextView text = view.findViewById(R.id.question);
        text.setText(questionId);

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
