package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.StealthNotificationStyle
import com.example.data.repository.SettingsRepository
import com.example.trigger.EmergencyTriggerHandler
import com.example.trigger.TriggerDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EmergencyGuardService : Service() {

    private var sensorManager: SensorManager? = null
    private var triggerDetector: TriggerDetector? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        triggerDetector = TriggerDetector(
            onTrigger = { triggerType ->
                EmergencyTriggerHandler.handleTrigger(this, triggerType)
            },
            onShakeProgress = { deltaG ->
                _currentShakeG.value = deltaG
            }
        )

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SilentGuard:SensorGuardWakeLock")
        wakeLock?.acquire(24 * 60 * 60 * 1000L) // 24h safety limit

        registerSensors()

        // Observe settings changes
        serviceScope.launch {
            SettingsRepository.getInstance(this@EmergencyGuardService).config.collectLatest { config ->
                triggerDetector?.let { detector ->
                    detector.isFlipEnabled = config.flipEnabled
                    detector.isShakeEnabled = config.shakeEnabled
                    detector.shakeThreshold = config.shakeSensitivity
                }

                if (!config.isGuardEnabled) {
                    stopSelf()
                } else {
                    val notification = buildGuardNotification(config.stealthNotification)
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(NOTIFICATION_ID, notification)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val config = SettingsRepository.getInstance(this).config.value
        val notification = buildGuardNotification(config.stealthNotification)
        startForeground(NOTIFICATION_ID, notification)
        isGuardRunning = true
        _guardActiveState.value = true
        return START_STICKY
    }

    private fun registerSensors() {
        val detector = triggerDetector ?: return
        val sm = sensorManager ?: return

        val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accel != null) {
            sm.registerListener(detector, accel, SensorManager.SENSOR_DELAY_NORMAL)
        }

        val gravity = sm.getDefaultSensor(Sensor.TYPE_GRAVITY)
        if (gravity != null) {
            sm.registerListener(detector, gravity, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun buildGuardNotification(style: StealthNotificationStyle): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, text) = when (style) {
            StealthNotificationStyle.BATTERY_SAVER -> {
                Pair("Android Battery Care", "Monitoring system power and performance")
            }
            StealthNotificationStyle.DEVICE_SECURITY -> {
                Pair("Device Protection", "Security sensors active")
            }
            StealthNotificationStyle.SILENT_GUARD -> {
                Pair("SilentGuard Active", "Flip and shake sensors monitoring for emergency")
            }
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingOpen)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SilentGuard Sensor Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Background sensor monitoring for gestures"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isGuardRunning = false
        _guardActiveState.value = false
        sensorManager?.unregisterListener(triggerDetector)
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val CHANNEL_ID = "silentguard_sensor_channel"
        const val NOTIFICATION_ID = 2002

        var isGuardRunning: Boolean = false
            private set

        private val _guardActiveState = MutableStateFlow(false)
        val guardActiveState: StateFlow<Boolean> = _guardActiveState.asStateFlow()

        private val _currentShakeG = MutableStateFlow(0f)
        val currentShakeG: StateFlow<Float> = _currentShakeG.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, EmergencyGuardService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, EmergencyGuardService::class.java)
            context.stopService(intent)
        }
    }
}
