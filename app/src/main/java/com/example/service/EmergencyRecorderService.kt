package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.EmergencyRecording
import com.example.data.model.StealthNotificationStyle
import com.example.data.repository.RecordingRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RecorderState(
    val isRecording: Boolean = false,
    val durationSeconds: Long = 0,
    val triggerSource: String = "",
    val currentFileName: String = "",
    val currentFilePath: String = ""
)

class EmergencyRecorderService : Service() {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var recordingStartTime: Long = 0
    private var triggerSource: String = "Manual Panic"
    private var actionType: String = ""

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_RECORDING

        when (action) {
            ACTION_START_RECORDING -> {
                if (!isRecordingRunning) {
                    triggerSource = intent?.getStringExtra(EXTRA_TRIGGER_SOURCE) ?: "Emergency Trigger"
                    actionType = intent?.getStringExtra(EXTRA_ACTION_TYPE) ?: ""
                    startRecording()
                }
            }
            ACTION_STOP_RECORDING -> {
                stopRecording()
            }
            ACTION_TOGGLE_RECORDING -> {
                if (isRecordingRunning) {
                    stopRecording()
                } else {
                    triggerSource = intent?.getStringExtra(EXTRA_TRIGGER_SOURCE) ?: "Quick Panic"
                    startRecording()
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun startRecording() {
        try {
            val settingsRepo = SettingsRepository.getInstance(this)
            val config = settingsRepo.config.value
            val recordingRepo = RecordingRepository.getInstance(this)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "emergency_$timeStamp.m4a"
            val file = File(recordingRepo.getRecordingsDirectory(), fileName)
            currentOutputFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            recordingStartTime = System.currentTimeMillis()
            isRecordingRunning = true

            _recorderState.value = RecorderState(
                isRecording = true,
                durationSeconds = 0,
                triggerSource = triggerSource,
                currentFileName = fileName,
                currentFilePath = file.absolutePath
            )

            // Start foreground notification immediately
            val notification = buildStealthNotification(config.stealthNotification, 0)
            startForeground(NOTIFICATION_ID, notification)

            // Start timer & auto-stop counter
            timerJob?.cancel()
            timerJob = serviceScope.launch {
                var seconds = 0L
                val maxMinutes = config.maxRecordingDurationMinutes
                val maxSeconds = if (maxMinutes > 0) maxMinutes * 60L else Long.MAX_VALUE

                while (isActive && isRecordingRunning) {
                    delay(1000L)
                    seconds++
                    _recorderState.value = _recorderState.value.copy(durationSeconds = seconds)

                    // Update notification periodically
                    if (seconds % 5L == 0L) {
                        val updatedNotification = buildStealthNotification(config.stealthNotification, seconds)
                        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        manager.notify(NOTIFICATION_ID, updatedNotification)
                    }

                    if (seconds >= maxSeconds) {
                        stopRecording()
                        break
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            stopRecording()
        }
    }

    private fun stopRecording() {
        timerJob?.cancel()
        timerJob = null

        val duration = if (recordingStartTime > 0) {
            (System.currentTimeMillis() - recordingStartTime) / 1000L
        } else 0L

        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
        }

        val file = currentOutputFile
        if (file != null && file.exists() && file.length() > 0) {
            val recordingRepo = RecordingRepository.getInstance(this)
            val recording = EmergencyRecording(
                fileName = file.name,
                filePath = file.absolutePath,
                timestamp = recordingStartTime.takeIf { it > 0 } ?: System.currentTimeMillis(),
                durationSeconds = duration,
                fileSizeBytes = file.length(),
                triggerSource = triggerSource,
                notes = "Recorded via $triggerSource"
            )

            serviceScope.launch(Dispatchers.IO) {
                recordingRepo.saveRecording(recording)
            }
        }

        isRecordingRunning = false
        _recorderState.value = RecorderState(isRecording = false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildStealthNotification(style: StealthNotificationStyle, seconds: Long): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, EmergencyRecorderService::class.java).apply {
            action = ACTION_STOP_RECORDING
        }
        val pendingStop = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedDuration = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60)

        val (title, text) = when (style) {
            StealthNotificationStyle.BATTERY_SAVER -> {
                Pair("Android Battery Care", "Optimizing background power efficiency")
            }
            StealthNotificationStyle.DEVICE_SECURITY -> {
                Pair("System Security Service", "Integrity monitor active")
            }
            StealthNotificationStyle.SILENT_GUARD -> {
                Pair("SilentGuard Emergency Recording", "Recording in progress ($formattedDuration)")
            }
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingOpen)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", pendingStop)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Emergency Recording Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent background emergency recording service"
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
        if (isRecordingRunning) {
            stopRecording()
        }
    }

    companion object {
        const val ACTION_START_RECORDING = "com.example.service.action.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.example.service.action.STOP_RECORDING"
        const val ACTION_TOGGLE_RECORDING = "com.example.service.action.TOGGLE_RECORDING"

        const val EXTRA_TRIGGER_SOURCE = "extra_trigger_source"
        const val EXTRA_ACTION_TYPE = "extra_action_type"

        const val CHANNEL_ID = "emergency_recorder_channel"
        const val NOTIFICATION_ID = 1001

        var isRecordingRunning: Boolean = false
            private set

        private val _recorderState = MutableStateFlow(RecorderState())
        val recorderState: StateFlow<RecorderState> = _recorderState.asStateFlow()
    }
}
