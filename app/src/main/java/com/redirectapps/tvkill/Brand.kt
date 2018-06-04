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
 * along with this program.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package com.redirectapps.tvkill

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class Brand (val designation: String, val patterns: Array<Pattern>, val mute: Pattern) {
    // TODO: refactor this/ remove this
    var dotransmit = true

    //This method transmits all of the brands off-patterns
    fun kill(c: Context) {
        for (r in patterns) {
            r.send(c)

            if (patterns.size > 1) {
                wait(c)
            }
        }
    }

    //This method transmits the brands mute-pattern
    fun mute(c: Context) {
        mute.send(c)
    }

    //This method returns true if the brand has a mute-pattern
    // TODO: remove this
    fun hasMute(): Boolean {
        return true
    }

    companion object {

        //This Method transmits all off-patterns of all brands
        fun killAll(c: Context) {
            //Check if additional patterns shall be transmitted
            var depth = 1
            val preferences = PreferenceManager.getDefaultSharedPreferences(c)
            if (preferences.getBoolean("depth", false)) {
                //TODO: determine longest brand-pattern-array
                depth = 2
            }
            //Transmit all patterns
            for (i in 0 until depth) {
                for (b in BrandContainer.allBrands) {
                    if (b.dotransmit) {
                        if (i < b.patterns.size) {
                            b.patterns[i].send(c)
                            wait(c)
                        }
                    }
                }
            }
        }

        //This Method transmits the mute-patterns of all brands
        fun muteAll(c: Context) {
            for (b in BrandContainer.allBrands) {
                if (b.hasMute() && b.dotransmit) {
                    b.mute.send(c)
                    wait(c)
                }
            }
        }

        //Wait for a certain time to avoid a misinterpretation of the commands when they are sent succecevly
        private fun wait(c: Context) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(c)
            try {
                Thread.sleep(java.lang.Long.parseLong(preferences.getString("delay", "0")))
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }

        }
    }


}
