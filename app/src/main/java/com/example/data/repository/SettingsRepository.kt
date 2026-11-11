package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.StealthNotificationStyle
import com.example.data.model.TriggerAction
import com.example.data.model.TriggerConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("silent_guard_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<TriggerConfig> = _config.asStateFlow()

    private fun loadConfig(): TriggerConfig {
        return TriggerConfig(
            isGuardEnabled = prefs.getBoolean("is_guard_enabled", true),
            flipEnabled = prefs.getBoolean("flip_enabled", true),
            shakeEnabled = prefs.getBoolean("shake_enabled", true),
            shakeSensitivity = prefs.getFloat("shake_sensitivity", 16.0f),
            volumeUp2xEnabled = prefs.getBoolean("vol_up_2x", false),
            volumeUp3xEnabled = prefs.getBoolean("vol_up_3x", true),
            volumeDown2xEnabled = prefs.getBoolean("vol_down_2x", false),
            volumeDown3xEnabled = prefs.getBoolean("vol_down_3x", true),
            selectedAction = try {
                TriggerAction.valueOf(prefs.getString("selected_action", TriggerAction.SILENT_MIC_RECORDING.name) ?: TriggerAction.SILENT_MIC_RECORDING.name)
            } catch (e: Exception) {
                TriggerAction.SILENT_MIC_RECORDING
            },
            stealthNotification = try {
                StealthNotificationStyle.valueOf(prefs.getString("stealth_notification", StealthNotificationStyle.BATTERY_SAVER.name) ?: StealthNotificationStyle.BATTERY_SAVER.name)
            } catch (e: Exception) {
                StealthNotificationStyle.BATTERY_SAVER
            },
            hapticPulseOnStart = prefs.getBoolean("haptic_pulse", true),
            maxRecordingDurationMinutes = prefs.getInt("max_duration_mins", 5)
        )
    }

    fun updateConfig(update: (TriggerConfig) -> TriggerConfig) {
        val newConfig = update(_config.value)
        prefs.edit().apply {
            putBoolean("is_guard_enabled", newConfig.isGuardEnabled)
            putBoolean("flip_enabled", newConfig.flipEnabled)
            putBoolean("shake_enabled", newConfig.shakeEnabled)
            putFloat("shake_sensitivity", newConfig.shakeSensitivity)
            putBoolean("vol_up_2x", newConfig.volumeUp2xEnabled)
            putBoolean("vol_up_3x", newConfig.volumeUp3xEnabled)
            putBoolean("vol_down_2x", newConfig.volumeDown2xEnabled)
            putBoolean("vol_down_3x", newConfig.volumeDown3xEnabled)
            putString("selected_action", newConfig.selectedAction.name)
            putString("stealth_notification", newConfig.stealthNotification.name)
            putBoolean("haptic_pulse", newConfig.hapticPulseOnStart)
            putInt("max_duration_mins", newConfig.maxRecordingDurationMinutes)
            apply()
        }
        _config.value = newConfig
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
