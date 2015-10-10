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
import android.os.Build;


public class Pattern {

    private int[] pattern;
    private int frequency;
    private boolean converted=false;

    //This method converts the remotes pattern, if this has not been done yet, and passes the converted pattern to the initiate-method.
    public void send(Context c) {

        if (!converted) {
            pattern = convert(pattern, frequency);
            converted = true;
        }
        initiate(c);

    }

    //This method initiates the remote's transmission.
    private void initiate(Context c) {
        Transmitter remote = new Transmitter(frequency, pattern);
        remote.transmit(c);
    }


    protected Pattern(int frequency, int[] pattern){
        this.frequency=frequency;
        this.pattern=pattern;
    }



    /*
    * The following methods preform calculations on the IR-codes in order to make them work on certain devices.
    *
    * Devices running on Android Lollipop or newer and HTC devices have to be converted with formula 1
    * Certain Samsung devices have to be converted with formula 2,depending on their Android version
    * Older devices including Samsung devices running on an android version older than 4.4.3 do not need to be converted, in that case, this method simply returns the input data
    *
    * Thanks a lot to Dwebtron from StackOverflow (https://stackoverflow.com/users/1042362), these methods where inspired by his answer, which really helped uas a lot: https://stackoverflow.com/a/28934938
    *
     */
    private static int[] convert(int[] irData, int frequency) {

        //Determine the conversion type
        int formula = conversionType();

        if (formula != 0) {
            for (int i = 0; i < irData.length; i++) {
                switch (formula) {
                    case 1:
                        irData[i] = irData[i] * (1000000 / frequency);
                        break;
                    case 2:
                        irData[i] = (int) Math.ceil(irData[i] * 26.27272727272727); //converted as suggested by Samsung: http://developer.samsung.com/android/technical-docs/Workaround-to-solve-issues-with-the-ConsumerIrManager-in-Android-version-lower-than-4-4-3-KitKat
                        break;
                }
            }
        }
        return irData;
    }

    //This Method determines with conversion-algorithm shall be used (see the comment above the convert-method for more details)
    private static int conversionType() {

        //HTC devices
        if (Build.MANUFACTURER.equalsIgnoreCase("HTC")) {
            return 1;
        }

        //Devices running on Android Lollipop (Android 5.0.1 (API 21)) and beyond
        if (Build.VERSION.SDK_INT >= 21) {
            return 1;
        }

        //Samsung devices running on anything lower than Android 5
        if (Build.MANUFACTURER.equalsIgnoreCase("SAMSUNG")) {
            if (Build.VERSION.SDK_INT >= 19) {
                int lastIdx = Build.VERSION.RELEASE.lastIndexOf(".");
                int VERSION_MR = Integer.valueOf(Build.VERSION.RELEASE.substring(lastIdx + 1));
                if (VERSION_MR < 3) {
                    // Samsung devices with Android-version before Android 4.4.2
                    //-> no calculations required
                    return 0;
                } else {
                    // Later version of Android 4.4.3
                    //-> use the special Samsung-formula
                    return 2;
                }
            }
        }

        //no calculations are required for older devices
        //-> return 0 by default
        return 0;
    }


}
