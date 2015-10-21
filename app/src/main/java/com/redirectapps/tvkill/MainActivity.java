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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.ConsumerIrManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends Activity {

    public static boolean repetitiveModeRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize the TabLayout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        TabLayout.Tab individualMode = tabLayout.newTab().setText(R.string.menu_specific_devices).setTag(new IndividualremoteFragment());
        TabLayout.Tab universalRemote = tabLayout.newTab().setText(R.string.menu_universal_mode).setTag(new UniversalmodeFragment());
        TabLayout.Tab repetitiveMode = tabLayout.newTab().setText(R.string.menu_repetitive_mode).setTag(new RepetitiveModeFragment());
        tabLayout.addTab(individualMode);
        tabLayout.addTab(universalRemote, true);
        tabLayout.addTab(repetitiveMode);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                displayFragment((Fragment) tab.getTag(), Integer.toString(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //Avoid screen rotation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        //Display the Startup-Fragment
        displayFragment(new UniversalmodeFragment(),"1");

        //Check for the IR-emitter
        ConsumerIrManager  IR = (ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);
        if (IR.hasIrEmitter()) {
            //Inform the user about the presence of his IR-emitter
            Toast.makeText(getApplicationContext(),R.string.toast_found,Toast.LENGTH_SHORT).show();
        }
        else {
            //Display a Dialog that tells the user to buy a different phone
            AlertDialog alertDialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle(R.string.blaster_dialog_title);
            builder.setMessage(R.string.blaster_dialog_body);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setNeutralButton(R.string.learn_more, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.blaster_dialog_more_blaster_url)));
                    startActivity(browserIntent);
                    finish();
                }
            });
            alertDialog=builder.create();
            alertDialog.show();
        }

    }


    //This method initiates the transmission and displays a progress Dialog
    public static void kill(Context c) {

        if (repetitiveModeRunning) {
            //Show the repetitiveModeActiveDialog
            repetitiveModeActiveDialog(c);
        }else {
            final Context context = c;
            Thread transmit;

            try {
                //Show a progress dialog and transmit all patterns
                final ProgressDialog transmittingInfo = getProgressDialog(c);
                transmit = new Thread() {
                    public void run() {
                        Brand.killAll(context);
                        transmittingInfo.dismiss();
                    }
                };
            }catch (android.view.WindowManager.BadTokenException e)  {
                //Transmit all patterns without displaying a progress dialog
                transmit = new Thread() {
                    public void run() {
                        Brand.killAll(context);
                    }
                };
            }

            transmit.start();

        }
    }

    //This method is called when the OFF-button is clicked. It simply calls the kill-method.
    public void off(View v){
        kill(this);
    }

    //This method returns a ProgressDialog
    public static ProgressDialog getProgressDialog(Context c) {
        return ProgressDialog.show(c, c.getString(R.string.pd_transmission_in_progress), c.getString(R.string.pd_please_wait), true, false);
    }


    //This method is called when the repetitive-button is clicked. It either starts or stops the RepetitiveModeService depending on if it is running or not.
    public void repetitiveMode(View v) {
        Intent RepetitiveIntent = new Intent(this, RepetitiveModeService.class);
        if (repetitiveModeRunning) {
            stopService(RepetitiveIntent);
            setRepetitiveButton(false);
        }else{
            startService(RepetitiveIntent);
            setRepetitiveButton(true);
        }
    }

    //This Method stops the repetitive-mode
    private static void stopRepetitiveMode (Context c) {
        Intent Intent = new Intent(c, RepetitiveModeService.class);
        c.stopService(Intent);
    }

    //This method switches the design of the repetitive-mode-button
    public void setRepetitiveButton (Boolean running) {
        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.repetitive_mode_button);
        if (running) {
            button.setImageDrawable(new BitmapDrawable(getResources(),BitmapFactory.decodeResource(getResources(),R.drawable.ic_stop_black_48dp)));
            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.stopred)));
        }
        else {
            button.setImageDrawable(new BitmapDrawable(getResources(),BitmapFactory.decodeResource(getResources(),R.drawable.ic_play_arrow_black_48dp)));
            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.startgreen)));
        }
    }

    //This method updates the design of the repetitive-mode-button
    public void updateRepetitiveButton() {
        setRepetitiveButton(repetitiveModeRunning);
    }


    //This Method displays a dialog that warns the user about the running repetitive-mode
    public static void repetitiveModeActiveDialog(final Context c) {
        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setCancelable(true);
        builder.setTitle(R.string.mode_running);
        builder.setMessage(R.string.dialog_running_body);
        builder.setPositiveButton(R.string.dialog_running_stop_mode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopRepetitiveMode(c);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog=builder.create();
        alertDialog.show();
    }

    //This method displays a specific help-dialog for the fragment that is currently displayed
    public void showHelp(View v) {
        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        switch (getFragmentManager().findFragmentById(R.id.fragment_container).getTag()) {
            case "0":
                builder.setTitle(R.string.menu_specific_devices);
                builder.setMessage(R.string.help_individual_mode);
                break;
            case "1":
                builder.setTitle(R.string.menu_universal_mode);
                builder.setMessage(R.string.help_universal_mode);
                break;
            case "2":
                builder.setTitle(R.string.menu_repetitive_mode);
                builder.setMessage(R.string.help_repetitive_mode);
        }
        builder.setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

    //This method is called, when the settings button is clicked. It starts the preferences-activity
    public void openSettings(View v) {
        Intent intent = new Intent(this,Preferences.class);
        startActivity(intent);
    }


    //This method displays a fragment
    void displayFragment (Fragment fragment, String tag) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.commit();
    }



    @Override
    protected void onDestroy() {

        //Stop the service
        stopRepetitiveMode(this);

        super.onDestroy();
    }
}
