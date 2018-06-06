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