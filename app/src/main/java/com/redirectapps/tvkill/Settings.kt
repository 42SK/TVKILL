package com.redirectapps.tvkill

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class Settings private constructor(context: Context) {
    companion object {
        private var settings: Settings? = null

        @Synchronized
        fun with(context: Context): Settings {
            if (settings == null) {
                settings = Settings(context.applicationContext)
            }

            return settings!!
        }

        private val PREF_MUTE = "show_mute"
        private val PREF_ADDITIONAL_PATTERNS = "depth"
    }

    private val showMuteInternal = MutableLiveData<Boolean>()
    private val additionalPatternsInternal = MutableLiveData<Boolean>()

    val showMute: LiveData<Boolean> = showMuteInternal
    val additionalPatterns: LiveData<Boolean> = additionalPatternsInternal

    init {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        showMuteInternal.value = preferences.getBoolean(PREF_MUTE, false)
        additionalPatternsInternal.value = preferences.getBoolean(PREF_ADDITIONAL_PATTERNS, false)

        preferences.registerOnSharedPreferenceChangeListener { _, key ->
            if (PREF_MUTE == key) {
                showMuteInternal.setValue(preferences.getBoolean(PREF_MUTE, false))
            } else if (PREF_ADDITIONAL_PATTERNS == key) {
                additionalPatternsInternal.setValue(preferences.getBoolean(PREF_ADDITIONAL_PATTERNS, false))
            } else {
                // ignore
            }
        }
    }
}
