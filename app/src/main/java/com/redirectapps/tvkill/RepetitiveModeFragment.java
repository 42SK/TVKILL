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
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.util.ArrayList;


public class RepetitiveModeFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.repetitive_mode, container, false);

        Spinner spinner = (Spinner) view.findViewById(R.id.repetitiveBrandChooser);

        //Create an ArrayList that contains all the spinner's possible choices
        ArrayList<String> optionList = new ArrayList<String>();
        //The first option is to transmit patterns for all brands
        optionList.add(this.getString(R.string.allBrands));
        //One can also choose to transmit patterns for one brand only.
        Brand[] allBrands = BrandContainer.getAllBrands();
        for (Brand b : allBrands) {
            //Add the brand's name to the ArrayList
            optionList.add(b.getDesignation().toUpperCase());
        }

        //Create an ArrayAdapter using the optionList and the default layout
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(getActivity(),android.R.layout.simple_spinner_item,optionList);
        //Use the default layout for the spinner's menu
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Attach the Adapter to the spinner
        spinner.setAdapter(arrayAdapter);
        //Attach the onItemSelectedListener
        spinner.setOnItemSelectedListener(this);

        return view;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Update the repetitiveModeBrand in MainActivity when the user selects an option
        MainActivity.repetitiveModeBrand=position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onResume() {
        super.onResume();

        //Set the design of the repetitive-button
        ((MainActivity)getActivity()).updateRepetitiveButton();

    }
}
