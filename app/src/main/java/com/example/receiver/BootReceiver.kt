package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.repository.SettingsRepository
import com.example.service.EmergencyGuardService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val settingsRepo = SettingsRepository.getInstance(context)
            if (settingsRepo.config.value.isGuardEnabled) {
                EmergencyGuardService.start(context)
            }
        }
    }
}
