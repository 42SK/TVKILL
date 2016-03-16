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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class Brand{

    private Pattern[] patterns;
    private Pattern mute;
    boolean dotransmit = true;
    private String designation;

    protected Brand (String designation, Pattern[] patterns) {
        this.patterns=patterns;
        this.designation=designation;
    }

    protected Brand (String designation, Pattern[] patterns, Pattern mute) {
        this.patterns=patterns;
        this.designation=designation;
        this.mute=mute;
    }

    //This method transmits all of the brands off-patterns
    public void kill(Context c) {
        for (Pattern r : patterns) {
            r.send(c);
            if (patterns.length>1)
            wait(c);
        }
    }

    //This method transmits the brands mute-pattern
    public void mute(Context c) {
        mute.send(c);
    }

    //This Method transmits all off-patterns of all brands
    public static void killAll(Context c) {
        //Check if additional patterns shall be transmitted
        int depth = 1;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        if (preferences.getBoolean("depth",false)) {
            //TODO: determine longest brand-pattern-array
            depth = 2;
        }
        //Transmit all patterns
        for (int i = 0; i<depth; i++) {
            for (Brand b : BrandContainer.getAllBrands()) {
                if (b.dotransmit) {
                    if (i<b.patterns.length) {
                        b.patterns[i].send(c);
                        wait(c);
                    }
                }
            }
        }
    }

    //This Method transmits the mute-patterns of all brands
    public static void muteAll(Context c) {
        for (Brand b : BrandContainer.getAllBrands()) {
            if (b.hasMute()&&b.dotransmit) {
                b.mute.send(c);
                wait(c);
            }
        }
    }

    //Wait for a certain time to avoid a misinterpretation of the commands when they are sent succecevly
    private static void wait(Context c) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        try {
            Thread.sleep(Long.parseLong(preferences.getString("delay","0")));
        } catch (InterruptedException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    //This method returns the Brands designation
    public String getDesignation() {
        return designation;
    }

    //This method returns true if the brand has a mute-pattern
    public boolean hasMute() {
        return (mute!=null);
    }



}
