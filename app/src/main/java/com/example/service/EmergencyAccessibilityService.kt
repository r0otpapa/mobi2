package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.data.repository.SettingsRepository
import com.example.trigger.EmergencyTriggerHandler
import com.example.trigger.HardwareKeySequenceTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EmergencyAccessibilityService : AccessibilityService() {

    private lateinit var keyTracker: HardwareKeySequenceTracker
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceActive.value = true

        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
            notificationTimeout = 100
        }
        serviceInfo = info

        keyTracker = HardwareKeySequenceTracker { triggerType ->
            EmergencyTriggerHandler.handleTrigger(this, triggerType)
        }

        // Keep key tracking configuration in sync with user settings
        serviceScope.launch {
            SettingsRepository.getInstance(this@EmergencyAccessibilityService).config.collectLatest { config ->
                keyTracker.volumeUp2xEnabled = config.volumeUp2xEnabled
                keyTracker.volumeUp3xEnabled = config.volumeUp3xEnabled
                keyTracker.volumeDown2xEnabled = config.volumeDown2xEnabled
                keyTracker.volumeDown3xEnabled = config.volumeDown3xEnabled
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return false

        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    val matched = keyTracker.onVolumeUpPressed()
                    // Don't consume the event unless an emergency trigger matched, so normal volume controls still work
                    return matched
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    val matched = keyTracker.onVolumeDownPressed()
                    return matched
                }
            }
        }
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used, this service is focused on hardware key events
    }

    override fun onInterrupt() {
        keyTracker.reset()
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceActive.value = false
    }

    companion object {
        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        fun isAccessibilityEnabled(context: Context): Boolean {
            val expectedServiceName = "${context.packageName}/${EmergencyAccessibilityService::class.java.canonicalName}"
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)

            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(expectedServiceName, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }

        fun openAccessibilitySettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
