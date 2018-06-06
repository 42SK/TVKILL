package com.redirectapps.tvkill

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils
import java.util.*
import kotlin.collections.HashSet

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
        private val PREF_WIDGET_IDS = "widget_ids"
    }

    private val showMuteInternal = MutableLiveData<Boolean>()
    private val additionalPatternsInternal = MutableLiveData<Boolean>()

    val showMute: LiveData<Boolean> = showMuteInternal
    val additionalPatterns: LiveData<Boolean> = additionalPatternsInternal
    private var appWidgetIds: Set<Int>
    val lock = Object()
    val preferences: SharedPreferences

    init {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)

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

        appWidgetIds = Collections.unmodifiableSet(
                HashSet<Int>(
                        preferences.getString(PREF_WIDGET_IDS, "")
                                .split(",")
                                .filter { !it.isEmpty() }
                                .map { it.toInt() }
                )
        )
    }

    fun getAppWidgetIds(): Set<Int> {
        return appWidgetIds
    }

    fun addAppWidgetIds(newIds: Collection<Int>) {
        synchronized(lock) {
            val newIdList = HashSet<Int>(appWidgetIds)
            newIdList.addAll(newIds)

            preferences.edit()
                    .putString(PREF_WIDGET_IDS, TextUtils.join(",", newIdList.map { it.toString() }))
                    .apply()

            this.appWidgetIds = Collections.unmodifiableSet(appWidgetIds)
        }
    }

    fun removeAppWidgetIds(newIds: Collection<Int>) {
        synchronized(lock) {
            val newIdList = HashSet<Int>(appWidgetIds)
            newIdList.removeAll(newIds)

            preferences.edit()
                    .putString(PREF_WIDGET_IDS, TextUtils.join(",", newIdList.map { it.toString() }))
                    .apply()

            this.appWidgetIds = Collections.unmodifiableSet(appWidgetIds)
        }
    }
}
