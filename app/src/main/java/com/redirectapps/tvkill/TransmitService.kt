/**
 * Copyright (C) 2018 Jonas Lochmann
 * Copyright (C) 2018 Sebastian Kappes
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
package com.redirectapps.tvkill

import android.app.*
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.*
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.support.v4.app.NotificationCompat
import android.support.v4.os.CancellationSignal
import com.redirectapps.tvkill.widget.UpdateWidget
import java.io.Serializable
import java.util.concurrent.Executors

class TransmitService: Service() {
    // helper method for starting
    companion object {
        private const val EXTRA_REQUEST = "request"
        private const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL = "report running in background"

        private val handler = Handler(Looper.getMainLooper())

        private val isAppInForeground = MutableLiveData<Boolean>()
        val status = MutableLiveData<TransmitServiceStatus>()

        init {
            status.value = null
            isAppInForeground.value = false
        }

        val subscribeIfRunning = object: LiveData<Void>() {
            override fun onActive() {
                super.onActive()

                isAppInForeground.value = true
            }

            override fun onInactive() {
                super.onInactive()

                isAppInForeground.value = false
            }
        }

        fun executeRequest(request: TransmitServiceRequest, context: Context) {
            context.startService(buildIntent(request, context))
        }

        fun buildIntent(request: TransmitServiceRequest, context: Context): Intent {
            return Intent(context, TransmitService::class.java)
                    .putExtra(EXTRA_REQUEST, request)
        }
    }

    private var verboseInformation: Boolean = false

    // detection if bound (used for showing/ hiding notification)
    private var cancel = CancellationSignal()
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var wakeLock: PowerManager.WakeLock
    private var isNotificationVisible = false
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private val statusObserver = Observer<TransmitServiceStatus> {
        updateNotification()
        UpdateWidget.updateAllWidgets(this)
    }
    private var stopped = false
    private var pendingRequests = 0

    override fun onCreate() {
        super.onCreate()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TransmitService")

        val cancelIntent = buildIntent(TransmitServiceCancelRequest, this)
        val pendingCancelIntent = PendingIntent.getService(this, PendingIntents.NOTIFICATION_CANCEL, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_power_settings_new_white_48dp)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                // this is set later (depending on the status)
                // .setContentTitle(getString(R.string.mode_running))
                .setOnlyAlertOnce(true)
                .setProgress(100, 0, true)
                .addAction(R.drawable.ic_clear_black_48dp, getString(R.string.stop), pendingCancelIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // setup notification channel (system ignores it if already registered)
            val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL,
                    getString(R.string.toast_transmission_initiated),
                    NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.setSound(null, null)
            channel.vibrationPattern = null
            channel.setShowBadge(false)
            channel.enableLights(false)

            notificationManager.createNotificationChannel(channel)
        }

        status.observeForever(statusObserver)
        isAppInForeground.observeForever {
            updateNotification()
        }

        wakeLock.acquire()
    }

    override fun onDestroy() {
        super.onDestroy()

        wakeLock.release()

        status.removeObserver(statusObserver)

        cancel()
        status.value = null
        stopped = true

        UpdateWidget.updateAllWidgets(this)

        stopForeground(true)

        //Dismiss the progress dialog (if present)
        if (MainActivity.progressDialog!=null) {
            MainActivity.progressDialog.dismiss()
            MainActivity.progressDialog = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // managing of current running things
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val request = intent!!.getSerializableExtra(EXTRA_REQUEST) as TransmitServiceRequest
        val cancel = this.cancel

        if (request is TransmitServiceSendRequest) {
            // there is no lock because the selected executor only executes one task per time

            pendingRequests++

            executor.submit {
                fun execute() {
                    if (request.brandName == null) {
                        if (request.action == TransmitServiceAction.Off) {
                            verboseInformation = getDefaultSharedPreferences(this).getBoolean("show_verbose",false)

                            // check if additional patterns should be transmitted
                            var depth = 1

                            if (Settings.with(this).additionalPatterns.value!!) {
                                depth = BrandContainer.allBrands.map { it.patterns.size }.max()!!
                            }

                            val numOfPatterns = BrandContainer.allBrands.sumBy {
                                Math.min(it.patterns.size, depth)
                            }
                            var transmittedPatterns = 0

                            // transmit all patterns
                            for (i in 0 until depth) {
                                for (brand in BrandContainer.allBrands) {
                                    if (cancel.isCanceled) {
                                        break
                                    }

                                    if (i < brand.patterns.size) {
                                        if (!request.forever) {
                                            status.postValue(TransmitServiceStatus(
                                                    request,
                                                    TransmitServiceProgress(transmittedPatterns++, numOfPatterns)
                                            ))
                                        }

                                        brand.patterns[i].send(this)
                                        Brand.wait(this)
                                    }
                                }
                            }
                        } else if (request.action == TransmitServiceAction.Mute) {
                            var transmittedPatterns = 0
                            val numOfPatterns = BrandContainer.allBrands.size

                            for (brand in BrandContainer.allBrands) {
                                if (cancel.isCanceled) {
                                    break
                                }

                                if (!request.forever) {
                                    status.postValue(TransmitServiceStatus(
                                            request,
                                            TransmitServiceProgress(transmittedPatterns++, numOfPatterns)
                                    ))
                                }

                                brand.mute(this)
                            }
                        } else {
                            throw IllegalStateException()
                        }
                    } else {
                        val brand = BrandContainer.brandByDesignation[request.brandName]

                        if (brand == null) {
                            throw IllegalStateException()
                        }

                        if (request.action == TransmitServiceAction.Off) {
                            brand.kill(this)
                        } else if (request.action == TransmitServiceAction.Mute) {
                            brand.mute(this)
                        } else {
                            throw IllegalStateException()
                        }
                    }
                }

                try {
                    status.postValue(TransmitServiceStatus(request, null))  // inform about this request

                    if (request.forever) {
                        while (!cancel.isCanceled) {
                            execute()
                        }
                    } else {
                        execute()
                    }
                } finally {
                    handler.post {
                        if (--pendingRequests == 0) {
                            status.value = null // nothing is running
                            stopSelf()
                        } else {
                            // status will be changed very soon
                        }
                    }
                }
            }
        } else if (request is TransmitServiceCancelRequest) {
            cancel()
        } else {
            throw IllegalStateException()
        }

        return START_NOT_STICKY
    }

    private fun cancel() {
        cancel.cancel()
        // create a new signal for next cancelling
        cancel = CancellationSignal()
    }

    private fun updateNotification() {
        if (stopped) {
            return
        }

        val request = status.value
        val appRunning = isAppInForeground.value

        if (appRunning!!) {
            if (isNotificationVisible) {
                stopForeground(true)
                isNotificationVisible = false
            }
        } else {
            if (request != null && request.request.forever) {
                notificationBuilder.setContentTitle(getString(R.string.mode_running))
            } else {
                notificationBuilder.setContentTitle(getString(R.string.toast_transmission_initiated))
            }

            if (request == null || request.progress == null) {
                notificationBuilder.setProgress(100, 0, true)
            } else {
                notificationBuilder.setProgress(request.progress.max, request.progress.current, false)

                //Also update the progress dialog (if present)
                if (MainActivity.progressDialog!=null) {
                    MainActivity.progressDialog.setMax(request.progress.max)
                    MainActivity.progressDialog.setProgress(request.progress.current+1)
                    if (verboseInformation)
                        MainActivity.progressDialog.setProgressNumberFormat(BrandContainer.allBrands[request.progress.current].designation.capitalize()+" (%1d/%2d)")
                }
            }

            if (isNotificationVisible) {
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
            } else {
                startForeground(NOTIFICATION_ID, notificationBuilder.build())
                isNotificationVisible = true
            }
        }
    }
}

sealed class TransmitServiceRequest(): Serializable
class TransmitServiceSendRequest(val action: TransmitServiceAction, val forever: Boolean, val brandName: String?): TransmitServiceRequest()
object TransmitServiceCancelRequest: TransmitServiceRequest()

enum class TransmitServiceAction {
    Off, Mute
}

data class TransmitServiceProgress(val current: Int, val max: Int)

class TransmitServiceStatus(val request: TransmitServiceSendRequest, val progress: TransmitServiceProgress?)