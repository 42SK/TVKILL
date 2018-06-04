package com.redirectapps.tvkill

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import android.support.v4.os.CancellationSignal
import java.io.Serializable
import java.util.concurrent.Executors

class TransmitService: Service() {
    // helper method for starting
    companion object {
        private const val EXTRA_REQUEST = "request"
        private const val NOTIFICATION_ID = 1

        val status = MutableLiveData<TransmitServiceStatus>()

        fun executeRequest(request: TransmitServiceRequest, context: Context) {
            context.startService(buildIntent(request, context))
        }

        fun buildIntent(request: TransmitServiceRequest, context: Context): Intent {
            return Intent(context, TransmitService::class.java)
                    .putExtra(EXTRA_REQUEST, request)
        }
    }

    // detection if bound (used for showing/ hiding notification)
    private val isBound = MutableLiveData<Boolean>()
    private var cancel = CancellationSignal()
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var wakeLock: PowerManager.WakeLock
    private var isNotificationVisible = false
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private val statusObserver = Observer<TransmitServiceStatus> {
        updateNotification()
    }

    init {
        isBound.value = false
        isBound.observeForever {
            updateNotification()
        }
    }

    override fun onCreate() {
        super.onCreate()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TransmitService")

        val cancelIntent = buildIntent(TransmitServiceCancelRequest, this)
        val pendingCancelIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = NotificationCompat.Builder(this)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_power_settings_new_white_48dp)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                // this is set later (depending on the status)
                // .setContentTitle(getString(R.string.mode_running))
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_clear_black_48dp, getString(R.string.stop), pendingCancelIntent)

        status.observeForever(statusObserver)

        wakeLock.acquire()
    }

    override fun onDestroy() {
        super.onDestroy()

        wakeLock.release()

        status.removeObserver(statusObserver)

        cancel()
        status.value = null
    }

    override fun onBind(p0: Intent?): IBinder? {
        isBound.value = true

        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isBound.value = false

        return true
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)

        isBound.value = true
    }

    // managing of current running things
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val request = intent!!.getSerializableExtra(EXTRA_REQUEST) as TransmitServiceRequest

        if (request is TransmitServiceSendRequest) {
            // there is no lock because the selected executor only executes one task per time

            executor.submit {
                fun execute() {
                    if (request.brandName == null) {
                        // TODO: support cancellation signal
                        // TODO: report progress (if forever == false)

                        if (request.action == TransmitServiceAction.Off) {
                            Brand.killAll(this)
                        } else if (request.action == TransmitServiceAction.Mute) {
                            Brand.muteAll(this)
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
                    status.postValue(null)  // nothing is running
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
        val request = status.value
        val bound = isBound.value

        if (bound!!) {
            if (isNotificationVisible) {
                stopForeground(true)
                isNotificationVisible = false
            }
        } else {
            if (request != null && request.request.forever) {
                notificationBuilder.setContentTitle(getString(R.string.mode_running))
            } else {
                notificationBuilder.setContentTitle(getString(R.string.mode_running_normal))
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