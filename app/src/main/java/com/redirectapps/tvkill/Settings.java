package com.redirectapps.tvkill;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
    private static Settings settings;

    public static synchronized Settings with(Context context) {
        if (settings == null) {
            settings = new Settings(context.getApplicationContext());
        }

        return settings;
    }

    private static final String PREF_MUTE = "show_mute";
    private final MutableLiveData<Boolean> showMuteInternal = new MutableLiveData<>();
    public final LiveData<Boolean> showMute = showMuteInternal;

    private Settings(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        showMuteInternal.setValue(preferences.getBoolean(PREF_MUTE, false));

        preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
                if (PREF_MUTE.equals(key)) {
                    showMuteInternal.setValue(preferences.getBoolean(PREF_MUTE, false));
                } else {
                    // ignore
                }
            }
        });
    }
}
