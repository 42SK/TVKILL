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

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class UniversalmodeFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.universal,container,false);

        //Set the visibility of the mute-button
        updateMuteButton(view);

        return view;
    }

    //This method updates the visibility of the mute-button
    private void updateMuteButton(View v) {
        //Check whether the mute-button shall be displayed
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("show_mute", false))
            v.findViewById(R.id.mute).setVisibility(View.VISIBLE);
        else v.findViewById(R.id.mute).setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();

        //Set the visibility of the mute-button
        updateMuteButton(getView());

    }
}
