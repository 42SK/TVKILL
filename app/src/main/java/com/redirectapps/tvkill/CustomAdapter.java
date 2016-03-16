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

import android.app.ProgressDialog;
import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class CustomAdapter extends ArrayAdapter<Brand> {

    //Check if the mute-option is enabled
    boolean muteEnabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("show_mute",false);

    public CustomAdapter(Context context, Brand[] brand) {
        super(context, R.layout.list_item, brand);
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        //Inflate the view
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View listItem = inflater.inflate(R.layout.list_item,parent,false);

        //Get the Brand for this position
        final Brand BrandItem = getItem(position);

        //Find the views
        TextView BrandName = (TextView) listItem.findViewById(R.id.designation);
        Button individualOFF = (Button)listItem.findViewById(R.id.individualOff);
        Button individualMUTE = (Button) listItem.findViewById(R.id.individualMute);

        //Set the brad's name
        BrandName.setText(BrandItem.getDesignation());
        //Set the action of the off-button
        individualOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.repetitiveModeRunning) {
                    //Show the repetitiveModeActiveDialog
                    MainActivity.repetitiveModeActiveDialog(getContext());
                }else {
                    //Show a progress dialog and transmit the brands patterns
                    final Context c = getContext();
                    final ProgressDialog transmittingInfo = MainActivity.getProgressDialog(c);
                    Thread transmit = new Thread() {
                        public void run() {
                            BrandItem.kill(c);
                            transmittingInfo.dismiss();
                        }
                    };
                    transmit.start();
                }
            }
        });

        //Set the action of the off-button and adjust the layout if the mute-option is enabled and available
        if (muteEnabled&&BrandItem.hasMute()) {
            //Change the visibility
            individualMUTE.setVisibility(View.VISIBLE);

            //Adjust the layout
            LinearLayout.LayoutParams newButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,1.5f);
            individualMUTE.setLayoutParams(newButtonParams);
            individualOFF.setLayoutParams(newButtonParams);

            //Set the button-text
            individualMUTE.setText(R.string.mute);

            //Set the action
            individualMUTE.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (MainActivity.repetitiveModeRunning) {
                        //Show the repetitiveModeActiveDialog
                        MainActivity.repetitiveModeActiveDialog(getContext());
                    }else {
                        //Show a progress dialog and transmit the brands mute-pattern
                        final Context c = getContext();
                        final ProgressDialog transmittingInfo = MainActivity.getProgressDialog(c);
                        Thread transmit = new Thread() {
                            public void run() {
                                BrandItem.mute(c);
                                transmittingInfo.dismiss();
                            }
                        };
                        transmit.start();
                    }
                }
            });
        }

        return listItem;
    }
}
