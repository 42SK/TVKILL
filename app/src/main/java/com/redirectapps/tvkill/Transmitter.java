/**
*   Copyright (C) 2015 Sebastian Kappes
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.redirectapps.tvkill;

import android.content.Context;
import android.hardware.ConsumerIrManager;


public class Transmitter {

    int frequency;
    int[] pattern;

    Transmitter(int frequency, int[] pattern) {
        this.frequency=frequency;
        this.pattern=pattern;
    }
    public void transmit(Context context) {
        ConsumerIrManager IR = (ConsumerIrManager) context.getSystemService(Context.CONSUMER_IR_SERVICE);

        IR.transmit(frequency, pattern);
    }
}
