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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.RemoteViews;

public class OffWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        //Create a BroadcastReceiver that initiates the transmission when called
        BroadcastReceiver kill = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //Transmit the patterns
                MainActivity.kill(context,'o');

            }
        };
        context.getApplicationContext().registerReceiver(kill, new IntentFilter("kill"));

        //Attach the BroadcastReceiver to a PendingIntent
        Intent receiverIntent = new Intent("kill");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,receiverIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        //The procedure inside the loop is performed with every off-widget connected to this provider
        for (int i= 0; i< appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];

            //Attach the PendingIntent to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.off_widget);
            views.setOnClickPendingIntent(R.id.killWidget,pendingIntent);

            //Update the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }
    }

}
