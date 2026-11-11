package com.example.trigger

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.model.TriggerType
import com.example.data.repository.SettingsRepository
import com.example.service.EmergencyRecorderService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EmergencyTriggerHandler {
    private val _triggerEvents = MutableSharedFlow<Pair<TriggerType, Long>>(extraBufferCapacity = 10)
    val triggerEvents: SharedFlow<Pair<TriggerType, Long>> = _triggerEvents.asSharedFlow()

    fun handleTrigger(context: Context, triggerType: TriggerType) {
        val settingsRepo = SettingsRepository.getInstance(context)
        val config = settingsRepo.config.value

        // Check master switch
        if (!config.isGuardEnabled) {
            _triggerEvents.tryEmit(Pair(triggerType, System.currentTimeMillis()))
            return
        }

        // Verify that this specific trigger type is enabled
        val isEnabled = when (triggerType) {
            TriggerType.FLIP -> config.flipEnabled
            TriggerType.SHAKE -> config.shakeEnabled
            TriggerType.VOLUME_UP_2X -> config.volumeUp2xEnabled
            TriggerType.VOLUME_UP_3X -> config.volumeUp3xEnabled
            TriggerType.VOLUME_DOWN_2X -> config.volumeDown2xEnabled
            TriggerType.VOLUME_DOWN_3X -> config.volumeDown3xEnabled
        }

        if (!isEnabled) {
            _triggerEvents.tryEmit(Pair(triggerType, System.currentTimeMillis()))
            return
        }

        // Emit for test UI
        _triggerEvents.tryEmit(Pair(triggerType, System.currentTimeMillis()))

        // Discreet pocket vibration to acknowledge trigger start without looking at screen
        if (config.hapticPulseOnStart) {
            vibrateDiscreetConfirmation(context)
        }

        // Launch silent recording service without turning on screen or opening app
        val serviceIntent = Intent(context, EmergencyRecorderService::class.java).apply {
            action = EmergencyRecorderService.ACTION_START_RECORDING
            putExtra(EmergencyRecorderService.EXTRA_TRIGGER_SOURCE, triggerType.displayName)
            putExtra(EmergencyRecorderService.EXTRA_ACTION_TYPE, config.selectedAction.name)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun vibrateDiscreetConfirmation(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Two gentle stealth pulses (80ms on, 80ms off, 120ms on)
                    val timings = longArrayOf(0, 80, 80, 120)
                    val amplitudes = intArrayOf(0, 180, 0, 200)
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 80, 80, 120), -1)
                }
            }
        } catch (e: Exception) {
            // In case of permission restriction, fail gracefully
        }
    }
}
