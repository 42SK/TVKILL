/**
 * Copyright (C) 2015 Sebastian Kappes
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redirectapps.tvkill;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.OpenableColumns;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;


public class Preferences extends PreferenceActivity {

    private final int OPEN_DOCUMENT_REQUEST_CODE = 0x33;
    private final String LAST_OPENED_URI_KEY = "last_opened_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPreferences = getDefaultSharedPreferences(MainActivity.getContext());

        Preference filePicker = this.findPreference("pattern_db_file");
        Preference customDbCheckbox = this.findPreference("custom_pattern_db_checkbox");

        // Display the user's current db file & set filePicker access
        if (sharedPreferences.contains(LAST_OPENED_URI_KEY)) {
            try {
                // Convert Uri to filename
                Uri documentUri = Uri.parse(sharedPreferences.getString(LAST_OPENED_URI_KEY, null));
                filePicker.setSummary(getFileName(documentUri));
            } catch (NullPointerException e) {
                e.printStackTrace();
                // Reset checkbox
                sharedPreferences.edit().putBoolean("custom_pattern_db_checkbox", false).apply();
            }
        }
        filePicker.setEnabled(sharedPreferences.getBoolean("custom_pattern_db_checkbox", false));

        // Ability to open a filepicker to choose a database file
        filePicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Intent to start openIntents File Manager
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                // Limit the type of files by mime-type: json only
                intent.setType("application/json");
                /*
                 * Because we'll want to read the data of whatever file is picked,
                 * we set [Intent.CATEGORY_OPENABLE] to ensure this will succeed.
                 * We don’t want to deal with virtual files, we need only real ones,
                 * i.e. file that contains bytes of data.
                 */
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                startActivityForResult(intent, OPEN_DOCUMENT_REQUEST_CODE);
                return true;
            }
        });

        customDbCheckbox.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Switch filepicker status according to the status of its checkbox
                filePicker.setEnabled((Boolean) newValue);
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            Uri documentUri = resultData.getData();
            if (documentUri == null)
                return;

            // Persist the permission across restarts to allow us to reopen the document
            MainActivity.getContext().getContentResolver().takePersistableUriPermission(
                    documentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            // Persist the Uri in preferences
            getDefaultSharedPreferences(MainActivity.getContext()).edit().putString(
                    LAST_OPENED_URI_KEY,
                    documentUri.toString()
            ).apply();

            this.findPreference("pattern_db_file").setSummary(getFileName(documentUri));
        }
    }

    // Return the name of the file for a given uri
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
