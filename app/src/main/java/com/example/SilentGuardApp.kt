package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.repository.SettingsRepository
import com.example.service.EmergencyGuardService
import com.example.service.EmergencyRecorderService

class SilentGuardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        // Auto-start sensor monitoring if enabled by user
        val settingsRepo = SettingsRepository.getInstance(this)
        if (settingsRepo.config.value.isGuardEnabled) {
            try {
                EmergencyGuardService.start(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val recorderChannel = NotificationChannel(
                EmergencyRecorderService.CHANNEL_ID,
                getString(R.string.notification_channel_recorder),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent background audio recording status"
                enableVibration(false)
                setSound(null, null)
                setShowBadge(false)
            }

            val guardChannel = NotificationChannel(
                EmergencyGuardService.CHANNEL_ID,
                getString(R.string.notification_channel_guard),
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Background sensor monitor status"
                enableVibration(false)
                setSound(null, null)
                setShowBadge(false)
            }

            manager.createNotificationChannel(recorderChannel)
            manager.createNotificationChannel(guardChannel)
        }
    }
}
