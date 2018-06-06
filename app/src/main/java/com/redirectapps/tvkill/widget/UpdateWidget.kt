/**
 * Copyright (C) 2015 Sebastian Kappes
 * Copyright (C) 2018 Jonas Lochmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package com.redirectapps.tvkill.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.redirectapps.tvkill.*

object UpdateWidget {
    fun updateAllWidgets(context: Context) {
        updateAllWidgets(context, AppWidgetManager.getInstance(context))
    }

    fun updateAllWidgets(context: Context, appWidgetManager: AppWidgetManager) {
        val powerOffPendingIntent = PendingIntent.getService(
                context,
                PendingIntents.WIDGET_POWER_OFF,
                TransmitService.buildIntent(
                        TransmitServiceSendRequest(
                                TransmitServiceAction.Off,
                                false,
                                null
                        ),
                        context
                ),
                PendingIntent.FLAG_UPDATE_CURRENT)

        val cancelPendingIntent = PendingIntent.getService(
                context,
                PendingIntents.WIDGET_CANCEL,
                TransmitService.buildIntent(
                        TransmitServiceCancelRequest,
                        context
                ),
                PendingIntent.FLAG_UPDATE_CURRENT)

        val isRunning = TransmitService.status.value != null

        for (appWidgetId in Settings.with(context).getAppWidgetIds()) {
            if (isRunning) {
                val views = RemoteViews(context.packageName, R.layout.off_widget_running)

                views.setOnClickPendingIntent(R.id.killWidget, cancelPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } else {
                val views = RemoteViews(context.packageName, R.layout.off_widget)
                views.setOnClickPendingIntent(R.id.killWidget, powerOffPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}