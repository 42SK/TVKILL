/**
 * Copyright (C) 2015 Sebastian Kappes
 * Copyright (C) 2021 Ysard
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
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.support.v4.provider.DocumentFile;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

import kotlin.text.Charsets;

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
        Uri documentUri = null;
        if (sharedPreferences.contains(LAST_OPENED_URI_KEY)) {
            try {
                // Convert Uri to filename
                documentUri = Uri.parse(sharedPreferences.getString(LAST_OPENED_URI_KEY, null));
                filePicker.setSummary(getFileName(documentUri));
            } catch (NullPointerException e) {
                e.printStackTrace();
                // Reset checkbox
                sharedPreferences.edit().putBoolean("custom_pattern_db_checkbox", false).apply();
            }
        }
        filePicker.setEnabled(sharedPreferences.getBoolean("custom_pattern_db_checkbox", false));

        // Ability to open a filepicker to choose a database file
        Uri finalDocumentUri = documentUri;
        filePicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Intent to start openIntents File Manager
                /* ACTION_OPEN_DOCUMENT is needed (contrary to ACTION_GET_CONTENT) because we need
                 * long-term, persistent permission on the uri, not a one-time thing.
                 * On some systems (like Xiaomi Redmi 4),
                 * it will only authorize listing on downloads directory...
                 */
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                // Limit the type of files by mime-type: json only
                // But... Not all platforms/file browser (?) support it
                if (MimeTypeMap.getSingleton().hasMimeType("application/json"))
                    intent.setType("application/json");
                else
                    intent.setType("*/*");

                /*
                 * Because we'll want to read the data of whatever file is picked,
                 * we set [Intent.CATEGORY_OPENABLE] to ensure this will succeed.
                 * We don’t want to deal with virtual files, we need only real ones,
                 * i.e. file that contains bytes of data.
                 */
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                if (Build.VERSION.SDK_INT > 25) {
                    // Set the desired initial location visible to user
                    // Could be useful on some platforms ?
                    DocumentFile file = DocumentFile.fromTreeUri(getApplicationContext(), finalDocumentUri);
                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, file.getUri());
                }

                startActivityForResult(intent, OPEN_DOCUMENT_REQUEST_CODE);
                return true;
            }
        });

        // Monitor changes in custom database checkbox
        customDbCheckbox.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean state = (Boolean) newValue;
                // Switch filepicker status according to the status of its checkbox
                filePicker.setEnabled(state);

                // DB previously selected or uncheck = restart the app/BrandContainer singleton
                // PS: There is no validity test on this file, this is useless since BrandContainer
                // will reset checkbox and file fields if it encounters a loading problem.
                if(!state || sharedPreferences.getString(LAST_OPENED_URI_KEY, null) != null) {
                    // Set pref manually before restart
                    sharedPreferences.edit().putBoolean("custom_pattern_db_checkbox", state).apply();
                    MainActivity.restart();
                }
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // content://com.android.providers.downloads.documents/document/<number>
            Uri documentUri = resultData.getData();
            if (documentUri == null)
                return;

            String filename = getFileName(documentUri);
            if (filename == null || !filename.endsWith(".json")) {
                Toast.makeText(getApplicationContext(), R.string.json_not_valid, Toast.LENGTH_SHORT).show();
                return;
            }

            // Test JSON validity
            try {
                InputStream stream = getContentResolver().openInputStream(documentUri);

                byte[] buffer = new byte[0];
                buffer = new byte[(int)stream.available()];
                stream.read(buffer);
                stream.close();

                String jsonData = new String(buffer, Charsets.UTF_8);
                JSONArray test = new JSONArray(jsonData);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.json_not_valid, Toast.LENGTH_SHORT).show();
                return;
            }

            // Persist the permission across restarts to allow us to reopen the document
            getContentResolver().takePersistableUriPermission(
                    documentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            // Persist the Uri in preferences
            getDefaultSharedPreferences(MainActivity.getContext()).edit().putString(
                    LAST_OPENED_URI_KEY,
                    documentUri.toString()
            ).apply();

            this.findPreference("pattern_db_file").setSummary(filename);

            // Restart the app/BrandContainer singleton
            MainActivity.restart();
        }
    }

    // Return the name of the file for a given uri
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            // projection: Optional but bad for performance if empty
            try (Cursor cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        return result;
    }
}
