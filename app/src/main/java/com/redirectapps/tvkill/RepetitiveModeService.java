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

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;


public class RepetitiveModeService extends IntentService {

    public RepetitiveModeService() {
        super("RepetitiveModeService");
    }

    private boolean run = true;
    NotificationManager mNotificationManager;
    NotificationCompat.Builder NotificationBuilder;
    int NotificationID = 1;
    BroadcastReceiver stopReceiver;
    int selectedBrand = MainActivity.repetitiveModeBrand;

    @Override
    protected void onHandleIntent(Intent intent) {


        //Set the running-boolean in MainActivity to true
        MainActivity.repetitiveModeRunning = true;


        //Display a Notification while this Service is running

        //Build the Notification
        NotificationBuilder = new NotificationCompat.Builder(this);
        NotificationBuilder.setOngoing(true);
        NotificationBuilder.setAutoCancel(false);
        NotificationBuilder.setPriority(Notification.PRIORITY_MAX);
        //NotificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
        NotificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        NotificationBuilder.setSmallIcon(R.drawable.ic_power_settings_new_white_48dp);
        NotificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        NotificationBuilder.setContentTitle(getString(R.string.mode_running));

        //Create a BroadcastReceiver that stops the service when called
        stopReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stopSelf();
            }
        };
        registerReceiver(stopReceiver, new IntentFilter("stop"));

        //Add a Action to the Notification that calls the BroadcastReceiver
        Intent receiverIntent = new Intent("stop");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,receiverIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationBuilder.addAction(R.drawable.ic_clear_black_48dp, getString(R.string.stop), pendingIntent);

        //Display the notification
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(NotificationID,NotificationBuilder.build());


        //Notify the user that the service has started
        Toast.makeText(getApplicationContext(),R.string.toast_mode_started,Toast.LENGTH_LONG).show();

        //Send Patterns until this service is stopped
        if (selectedBrand == 0)
        while (run) {
            //Send Patterns for all Brands
            Brand.killAll(this);
        }
        else {
            BrandContainer.getAllBrands();
            Brand b = BrandContainer.getAllBrands()[selectedBrand-1];
            while (run) {
                //Send the patterns of the selected brand
                b.kill(this);
            }
        }


    }

    @Override
    public void onDestroy() {

        //Make sure that the loop stops
        run = false;

        //Dismiss the notification
        mNotificationManager.cancel(NotificationID);

        //Unregister the receiver for the notification’s stop-intent
        unregisterReceiver(stopReceiver);

        //Set the running-boolean in MainActivity to false
        MainActivity.repetitiveModeRunning = false;

        //Inform the user that this service is stopping
        Toast.makeText(getApplicationContext(),R.string.toast_mode_stopped,Toast.LENGTH_LONG).show();

        super.onDestroy();
    }
}
