/**
 * Copyright (C) 2018 Jonas Lochmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
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

        private const val PREF_MUTE = "show_mute"
        private const val PREF_ADDITIONAL_PATTERNS = "depth"
        private const val PREF_WIDGET_IDS = "widget_ids"
        // the old value was saved as string, so the setting was renamed
        private const val PREF_DELAY_BETWEEN_PATTERNS = "delay_2"
        private const val PREF_SHOW_DETAILS = "show_details"
    }

    private val showMuteInternal = MutableLiveData<Boolean>()
    private val additionalPatternsInternal = MutableLiveData<Boolean>()
    private val delayBetweenPatternsInternal = MutableLiveData<Long>()
    private val showDetailsInternal = MutableLiveData<Boolean>()

    val showMute: LiveData<Boolean> = showMuteInternal
    val additionalPatterns: LiveData<Boolean> = additionalPatternsInternal
    val delayBetweenPatterns: LiveData<Long> = delayBetweenPatternsInternal
    val showDetails: LiveData<Boolean> = showDetailsInternal

    private var appWidgetIds: Set<Int>
    val lock = Object()
    val preferences: SharedPreferences

    init {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)

        showMuteInternal.value = preferences.getBoolean(PREF_MUTE, false)
        additionalPatternsInternal.value = preferences.getBoolean(PREF_ADDITIONAL_PATTERNS, false)
        delayBetweenPatternsInternal.value = preferences.getLong(PREF_DELAY_BETWEEN_PATTERNS, 0)
        showDetailsInternal.value = preferences.getBoolean(PREF_SHOW_DETAILS, false)

        appWidgetIds = Collections.unmodifiableSet(
                HashSet<Int>(
                        preferences.getString(PREF_WIDGET_IDS, "")
                                .split(",")
                                .filter { !it.isEmpty() }
                                .map { it.toInt() }
                )
        )
    }

    fun setShowMute(showMute: Boolean) {
        showMuteInternal.value = showMute

        preferences.edit()
                .putBoolean(PREF_MUTE, showMute)
                .apply()
    }

    fun setShowAdditionalPatterns(additionalPatterns: Boolean) {
        additionalPatternsInternal.value = additionalPatterns

        preferences.edit()
                .putBoolean(PREF_ADDITIONAL_PATTERNS, additionalPatterns)
                .apply()
    }

    fun setDelayBetweenPatterns(delay: Long) {
        delayBetweenPatternsInternal.value = delay

        preferences.edit()
                .putLong(PREF_DELAY_BETWEEN_PATTERNS, delay)
                .apply()
    }

    fun setShowDetails(showDetails: Boolean) {
        showDetailsInternal.value = showDetails

        preferences.edit()
                .putBoolean(PREF_SHOW_DETAILS, showDetails)
                .apply()
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

            this.appWidgetIds = Collections.unmodifiableSet(newIdList)
        }
    }

    fun removeAppWidgetIds(newIds: Collection<Int>) {
        synchronized(lock) {
            val newIdList = HashSet<Int>(appWidgetIds)
            newIdList.removeAll(newIds)

            preferences.edit()
                    .putString(PREF_WIDGET_IDS, TextUtils.join(",", newIdList.map { it.toString() }))
                    .apply()

            this.appWidgetIds = Collections.unmodifiableSet(newIdList)
        }
    }
}
