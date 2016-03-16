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

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;


public class IndividualremoteFragment extends ListFragment {

    private ArrayAdapter adapter;
    private boolean stopped = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        stopped=false;
        View view = inflater.inflate(R.layout.individualremote, container, false);
        adapter = new CustomAdapter(getActivity(),BrandContainer.getAllBrands());
        setListAdapter(adapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Make sure that the list style updates after changing the settings
        if(stopped)
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopped=true;
    }
}
