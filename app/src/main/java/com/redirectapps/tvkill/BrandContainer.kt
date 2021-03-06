/**
 * Copyright (C) 2015-2018 Sebastian Kappes
 * Copyright (C) 2018 Jonas Lochmann
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
 * along with this program.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package com.redirectapps.tvkill

import android.net.Uri
import android.preference.PreferenceManager
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.util.*

// Kotlin Singleton
object BrandContainer {
    /*
    Singleton that loads brands and their patterns from JSON asset:
    Brand object keys:
        - designation (String)
        - patterns (ArrayList<Pattern>)
        - mute (Pattern object)
    Pattern object keys:
        - frequency (int)
        - pattern (IntArray)

    TODO: Prettier deserialization could be made via projects like Klaxon or Gson...

    IR patterns are from the original TVKILL project, and from TV-B-Gone project.

    ---
    About TV-B-Gone IR codes conversion:
    Conversion program at https://github.com/glaukon-ariston/TV-B-Gone-Codes
    Converted IR database from https://github.com/shirriff/Arduino-TV-B-Gone
    According to https://github.com/42SK/TVKILL/wiki/How-to-add-IR-patterns-to-TV-KILL

    The constructor of the Pattern class requires two parameters:
      1. The pattern's frequency in Hertz
      2. The alternating on/off pattern in periods of the carrier frequency
    */

    private const val LAST_OPENED_URI_KEY = "last_opened_key"

    // Load JSON asset with patterns per brand
    fun loadJSONFromAsset(): String? {
        val jsonData: String? = try {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext())
            var stream: InputStream
            // Reload user's custom DB file
            if (sharedPreferences.getBoolean("custom_pattern_db_checkbox", false) && sharedPreferences.contains(LAST_OPENED_URI_KEY)) {
                try {
                    val documentUri = Uri.parse(sharedPreferences.getString(LAST_OPENED_URI_KEY, null))
                    stream = MainActivity.getContext().contentResolver.openInputStream(documentUri)
                } catch (e: Exception) {
                    e.printStackTrace();
                    when (e) {
                        // An error occurred: reset related preferences and load default DB
                        is IllegalArgumentException, is SecurityException, is FileNotFoundException, is NullPointerException -> {
                            // Reset Uri in prefs
                            sharedPreferences.edit().remove(LAST_OPENED_URI_KEY).apply()
                            // Disable custom DB in prefs
                            sharedPreferences.edit().putBoolean("custom_pattern_db_checkbox", false).apply()
                            // Fallback
                            stream = MainActivity.getContext().getAssets().open("brand_patterns.json")
                        }
                        else -> throw e
                    }
                }
            } else {
                // Load default DB
                stream = MainActivity.getContext().getAssets().open("brand_patterns.json")
            }
            val size = stream.available()
            val buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return jsonData
    }

    // Get a Pattern object based on the given JSON object
    fun buildPattern(jsonPattern: JSONObject): Pattern {

        val frequency = jsonPattern.getInt("frequency")
        val pattern = jsonPattern.getJSONArray("pattern")

        val numbers = IntArray(pattern.length())
        for (i_index in 0 until pattern.length()) {
            numbers[i_index] = pattern.optInt(i_index)
        }
        return Pattern(frequency, numbers)
    }

    // Load Brands and their patterns from JSON asset
    @JvmStatic
    val allBrands: Array<Brand> by lazy {

        val jsonData = loadJSONFromAsset()  // PS: can be null if file is not found...
        val brands = ArrayList<Brand>()
        val jsonBrands = JSONArray(jsonData)

        for (i in 0 until jsonBrands.length()) {
            try {
                val jsonBrand = jsonBrands.getJSONObject(i)

                // Get Off patterns
                val jsonOffPatterns = jsonBrand.getJSONArray("patterns")
                val offPatterns = ArrayList<Pattern>(jsonOffPatterns.length())
                for (j in 0 until jsonOffPatterns.length()) {
                    val jsonPattern = jsonOffPatterns.getJSONObject(j)
                    offPatterns.add(buildPattern(jsonPattern))
                }

                // Get Mute pattern
                var mutePattern: Pattern?;
                if (jsonBrand.has("mute")) {
                    val jsonMutePattern = jsonBrand.getJSONObject("mute")
                    mutePattern = buildPattern(jsonMutePattern)
                } else {
                    // Workaround if no Mute pattern
                    mutePattern = Pattern(0, IntArray(0))
                }

                // Finally, create Brand object
                val designation = jsonBrand.getString("designation")
                brands.add(Brand(designation, offPatterns.toTypedArray(), mutePattern))
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e("TVKILL", "Badly formatted pattern at index" + i.toString())
                continue
            }
        }
        brands.toTypedArray()
    }

    // Map brands by their names
    val brandByDesignation: Map<String, Brand> by lazy {
        val result = HashMap<String, Brand>()

        allBrands.forEach {
            result[it.designation] = it
        }

        Collections.unmodifiableMap(result)
    }
}
