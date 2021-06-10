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

import android.content.Context;
import android.hardware.ConsumerIrManager;

import com.sun.jna.Pointer;


public class Transmitter {

    private final Pattern pattern;
    public static Pointer tiqiaaUsbIr;
    public static ConsumerIrManager irManager;

    Transmitter(Pattern pattern) {
        this.pattern = pattern;
    }

    public void transmit(Context context) {

        if (tiqiaaUsbIr != null) {
            nativeWrapper.INSTANCE.transmit(tiqiaaUsbIr, pattern.frequency, pattern.pulsePattern, pattern.pulsePattern.length);
        } else {
            irManager.transmit(pattern.frequency, pattern.pattern);
        }
    }
}
