package com.github.muellerma.coffee

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import com.github.muellerma.coffee.appfunctions.ScreenAwakeState
import com.github.muellerma.coffee.tiles.CoffeeTile

/** Keeps the display awake until stopped manually, by timeout, or by locking the display. */
class ForegroundService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var wakeLock: PowerManager.WakeLock? = null
    private var screenOffReceiverRegistered = false

    private val stopAtDeadline = Runnable { stopSession() }
    private val refreshNotification = object : Runnable {
        override fun run() {
            if (!isRunning || deadline == null) return
            getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, createNotification())
            handler.postDelayed(this, NOTIFICATION_REFRESH_MILLIS)
        }
    }

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = stopSession()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(
            NOTIFICATION_ID,
            createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_OR_UPDATE -> {
                if (intent.hasExtra(EXTRA_DURATION_MINUTES)) {
                    sessionDurationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 0)
                }
                if (intent.hasExtra(EXTRA_ALLOW_DIMMING)) {
                    sessionAllowsDimming = intent.getBooleanExtra(EXTRA_ALLOW_DIMMING, true)
                }
                if (isRunning) {
                    acquireWakeLock()
                    restartTimer()
                } else {
                    startSession()
                }
            }
            ACTION_STOP -> stopSession()
            ACTION_TOGGLE -> if (isRunning) stopSession() else startSession()
            ACTION_NEXT_TIMEOUT -> {
                sessionDurationMinutes = null
                CoffeeSettings.selectNextTimeout(this, startAtFiveMinutes = !isRunning)
                if (isRunning) restartTimer() else startSession()
            }
            ACTION_RESTART_TIMEOUT -> if (isRunning) restartTimer() else startSession()
            ACTION_TOGGLE_DIMMING -> {
                sessionAllowsDimming = null
                CoffeeSettings.toggleDimming(this)
                if (isRunning) {
                    acquireWakeLock()
                    updateNotification()
                    CoffeeTile.updateListeningTiles()
                } else {
                    stopSession()
                }
            }
            else -> startSession()
        }
        return START_NOT_STICKY
    }

    private fun startSession() {
        if (isRunning) return

        acquireWakeLock()
        registerReceiver(
            screenOffReceiver,
            IntentFilter(Intent.ACTION_SCREEN_OFF),
            RECEIVER_NOT_EXPORTED
        )
        screenOffReceiverRegistered = true
        isRunning = true
        restartTimer()
    }

    @SuppressLint("WakelockTimeout")
    @Suppress("DEPRECATION")
    private fun acquireWakeLock() {
        releaseWakeLock()
        val level = if (currentAllowsDimming(this)) {
            PowerManager.SCREEN_DIM_WAKE_LOCK
        } else {
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK
        }
        wakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(level, "$packageName:keep-screen-on")
            .apply {
                setReferenceCounted(false)
                acquire()
            }
    }

    private fun restartTimer() {
        handler.removeCallbacks(stopAtDeadline)
        handler.removeCallbacks(refreshNotification)

        startedAt = SystemClock.elapsedRealtime()
        val timeout = currentDurationMinutes(this)
        deadline = timeout.takeIf { it > 0 }
            ?.let { startedAt + it * 60_000L }
        deadline?.let {
            handler.postDelayed(stopAtDeadline, (it - SystemClock.elapsedRealtime()).coerceAtLeast(0))
            handler.postDelayed(refreshNotification, NOTIFICATION_REFRESH_MILLIS)
        }
        updateNotification()
        CoffeeTile.updateListeningTiles()
    }

    private fun stopSession() {
        isRunning = false
        sessionDurationMinutes = null
        sessionAllowsDimming = null
        handler.removeCallbacksAndMessages(null)
        deadline = null
        releaseWakeLock()
        unregisterScreenOffReceiver()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        CoffeeTile.updateListeningTiles()
    }

    private fun releaseWakeLock() {
        val lock = wakeLock
        wakeLock = null
        if (lock?.isHeld == true) lock.release()
    }

    override fun onDestroy() {
        isRunning = false
        sessionDurationMinutes = null
        sessionAllowsDimming = null
        handler.removeCallbacksAndMessages(null)
        deadline = null
        releaseWakeLock()
        unregisterScreenOffReceiver()
        CoffeeTile.updateListeningTiles()
        super.onDestroy()
    }

    private fun unregisterScreenOffReceiver() {
        if (screenOffReceiverRegistered) {
            unregisterReceiver(screenOffReceiver)
            screenOffReceiverRegistered = false
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        getSystemService(NotificationManager::class.java).apply {
            deleteNotificationChannel(LEGACY_NOTIFICATION_CHANNEL_ID)
            createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = getString(R.string.notification_channel_description)
                    setShowBadge(false)
                    enableVibration(false)
                    enableLights(false)
                    setSound(null, null)
                }
            )
        }
    }

    private fun updateNotification() {
        if (isRunning) {
            getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, createNotification())
        }
    }

    private fun createNotification(): Notification {
        val progress = Notification.ProgressStyle()
        val remaining = deadline?.let { (it - SystemClock.elapsedRealtime()).coerceAtLeast(0) }
        if (remaining == null) {
            progress.setProgressIndeterminate(true)
        } else {
            val totalSeconds = ((deadline!! - startedAt) / 1_000).coerceAtLeast(1).toInt()
            val elapsedSeconds = (totalSeconds - remaining / 1_000).coerceIn(0, totalSeconds.toLong()).toInt()
            progress
                .addProgressSegment(
                    Notification.ProgressStyle.Segment(totalSeconds)
                        .setColor(getColor(R.color.coffee_brown))
                )
                .setProgress(elapsedSeconds)
        }

        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_twotone_free_breakfast_24_accent)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.tap_to_turn_off))
            .setContentIntent(serviceIntent(ACTION_STOP, 0))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setColor(getColor(R.color.coffee_brown))
            .setCategory(Notification.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            .setRequestPromotedOngoing(true)
            .setStyle(progress)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.ic_baseline_access_time_24),
                    getString(R.string.action_next),
                    serviceIntent(ACTION_NEXT_TIMEOUT, 1)
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.ic_baseline_refresh_24),
                    getString(R.string.action_restart),
                    serviceIntent(ACTION_RESTART_TIMEOUT, 2)
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.ic_baseline_brightness_medium_24),
                    getString(
                        if (currentAllowsDimming(this)) R.string.action_bright else R.string.action_dim
                    ),
                    serviceIntent(ACTION_TOGGLE_DIMMING, 3)
                ).build()
            )
            .apply {
                if (remaining == null) {
                    setShowWhen(false)
                    setShortCriticalText(getString(R.string.notification_chip_on))
                } else {
                    setWhen(System.currentTimeMillis() + remaining)
                    setShowWhen(true)
                    setUsesChronometer(true)
                    setChronometerCountDown(true)
                }
            }
            .build()
    }

    private fun serviceIntent(action: String, requestCode: Int): PendingIntent =
        PendingIntent.getForegroundService(
            this,
            requestCode,
            Intent(this, ForegroundService::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    companion object {
        const val ACTION_START_OR_UPDATE = "com.github.muellerma.coffee.action.START_OR_UPDATE"
        const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"
        const val EXTRA_ALLOW_DIMMING = "extra_allow_dimming"

        const val ACTION_TOGGLE = "com.github.muellerma.coffee.action.TOGGLE"
        private const val ACTION_STOP = "com.github.muellerma.coffee.action.STOP"
        const val ACTION_NEXT_TIMEOUT = "com.github.muellerma.coffee.action.NEXT_TIMEOUT"
        const val ACTION_RESTART_TIMEOUT = "com.github.muellerma.coffee.action.RESTART_TIMEOUT"
        const val ACTION_TOGGLE_DIMMING = "com.github.muellerma.coffee.action.TOGGLE_DIMMING"
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "screen_on_status_v3"
        private const val LEGACY_NOTIFICATION_CHANNEL_ID = "screen_on_status_v2"
        private const val NOTIFICATION_REFRESH_MILLIS = 60_000L

        @Volatile
        var isRunning: Boolean = false
            private set

        @Volatile
        var sessionDurationMinutes: Int? = null
            private set

        @Volatile
        var sessionAllowsDimming: Boolean? = null
            private set

        @Volatile
        var deadline: Long? = null
            private set

        @Volatile
        var startedAt: Long = 0L
            private set

        val remainingSeconds: Long?
            get() = if (isRunning) {
                deadline?.let { (it - SystemClock.elapsedRealtime()).coerceAtLeast(0) / 1000 }
            } else {
                null
            }

        fun currentAllowsDimming(context: Context): Boolean =
            sessionAllowsDimming ?: CoffeeSettings.allowsDimming(context)

        fun currentDurationMinutes(context: Context): Int =
            sessionDurationMinutes ?: CoffeeSettings.timeoutMinutes(context)

        fun currentState(context: Context): ScreenAwakeState {
            val mode = if (currentAllowsDimming(context)) "DIM" else "BRIGHT"
            return ScreenAwakeState(
                isAwake = isRunning,
                remainingSeconds = remainingSeconds,
                mode = mode
            )
        }

        fun startOrUpdate(
            context: Context,
            durationMinutes: Int?,
            allowDimming: Boolean?
        ): ScreenAwakeState {
            if (durationMinutes != null) {
                sessionDurationMinutes = durationMinutes
            }
            if (allowDimming != null) {
                sessionAllowsDimming = allowDimming
            }
            val intent = Intent(context, ForegroundService::class.java).apply {
                action = ACTION_START_OR_UPDATE
                if (durationMinutes != null) putExtra(EXTRA_DURATION_MINUTES, durationMinutes)
                if (allowDimming != null) putExtra(EXTRA_ALLOW_DIMMING, allowDimming)
            }
            context.startForegroundService(intent)

            val targetDuration = durationMinutes ?: currentDurationMinutes(context)
            val calculatedRemaining = if (targetDuration > 0) targetDuration * 60L else null
            val mode = if (allowDimming ?: currentAllowsDimming(context)) "DIM" else "BRIGHT"
            return ScreenAwakeState(
                isAwake = true,
                remainingSeconds = calculatedRemaining,
                mode = mode
            )
        }

        fun stop(context: Context): ScreenAwakeState {
            val intent = Intent(context, ForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startForegroundService(intent)
            sessionDurationMinutes = null
            sessionAllowsDimming = null
            val mode = if (CoffeeSettings.allowsDimming(context)) "DIM" else "BRIGHT"
            return ScreenAwakeState(
                isAwake = false,
                remainingSeconds = null,
                mode = mode
            )
        }
    }
}
