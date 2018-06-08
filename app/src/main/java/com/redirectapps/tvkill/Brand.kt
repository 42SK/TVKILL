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

class Brand (val designation: String, val patterns: Array<Pattern>, val mute: Pattern) {
    // this method transmits all of the brands off-patterns
    fun kill(c: Context) {
        for (r in patterns) {
            r.send(c)

            if (patterns.size > 1) {
                wait(c)
            }
        }
    }

    // this method transmits the brands mute-pattern
    fun mute(c: Context) {
        mute.send(c)
    }

    companion object {
        // wait for a certain time to avoid a misinterpretation of the commands when they are sent succecevly
        fun wait(c: Context) {
            try {
                Thread.sleep(Settings.with(c).delayBetweenPatterns.value!!)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}
