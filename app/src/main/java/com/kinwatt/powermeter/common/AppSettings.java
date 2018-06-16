package com.kinwatt.powermeter.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppSettings {
    private static final String KEY_QUESTIONARY_COMPLETED = "20180507_form";

    private static AppSettings instance;
    private SharedPreferences preferences;

    private Context context;

    private AppSettings(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static AppSettings getAppSettings(Context context) {
        if (instance == null) {
            instance = new AppSettings(context);
        }
        return instance;
    }

    public boolean isQuestionaryCompleted() {
        return preferences.getBoolean(KEY_QUESTIONARY_COMPLETED, false);
    }
    public void setQuestionaryCompleted(boolean completed) {
        preferences.edit().putBoolean(KEY_QUESTIONARY_COMPLETED, completed).apply();
    }
}
