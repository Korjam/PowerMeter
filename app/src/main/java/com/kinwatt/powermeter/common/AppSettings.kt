package com.kinwatt.powermeter.common

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class AppSettings private constructor(private val context: Context) {
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var isQuestionaryCompleted: Boolean
        get() = preferences.getBoolean(KEY_QUESTIONARY_COMPLETED, false)
        set(completed) = preferences.edit().putBoolean(KEY_QUESTIONARY_COMPLETED, completed).apply()

    companion object {
        private const val KEY_QUESTIONARY_COMPLETED = "20180507_form"

        private var instance: AppSettings? = null

        fun getAppSettings(context: Context): AppSettings {
            if (instance == null) {
                instance = AppSettings(context)
            }
            return instance!!
        }
    }
}
