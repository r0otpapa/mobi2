package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_recordings")
data class EmergencyRecording(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val timestamp: Long,
    val durationSeconds: Long,
    val fileSizeBytes: Long,
    val triggerSource: String, // "Flip Gesture", "Shake Gesture", "Volume Up (2x)", "Volume Up (3x)", "Volume Down (2x)", "Manual Panic"
    val notes: String = "",
    val locationTag: String = "" // Optional coordinates / location label
)

enum class TriggerType(val displayName: String, val description: String) {
    FLIP("Flip Face-Down", "Turn the phone face down quickly to trigger"),
    SHAKE("Shake Device", "Firmly shake the phone in any direction"),
    VOLUME_UP_2X("Volume Up (2x)", "Press Volume Up twice within 1.5s"),
    VOLUME_UP_3X("Volume Up (3x)", "Press Volume Up 3 times within 1.8s"),
    VOLUME_DOWN_2X("Volume Down (2x)", "Press Volume Down twice within 1.5s"),
    VOLUME_DOWN_3X("Volume Down (3x)", "Press Volume Down 3 times within 1.8s")
}

enum class TriggerAction(val title: String, val description: String) {
    SILENT_MIC_RECORDING("Discreet Mic Recording", "Silently records audio from microphone in background without turning on the screen or opening any app"),
    RECORD_WITH_LOCATION("Mic Recording + Location Tag", "Silently records audio and tags device location timestamp"),
    RECORD_WITH_DISCREET_SOS("Mic Recording + Subtle Morse Flash", "Records audio silently with subtle emergency SOS light pulses")
}

enum class StealthNotificationStyle(val title: String, val subtitle: String, val label: String) {
    BATTERY_SAVER("Android Battery Care", "Optimizing background power efficiency", "Disguised as Battery System"),
    DEVICE_SECURITY("System Security Service", "Device integrity and security active", "Disguised as Android System"),
    SILENT_GUARD("SilentGuard Active", "Emergency protection running in background", "Standard SilentGuard")
}

data class TriggerConfig(
    val isGuardEnabled: Boolean = true,
    val flipEnabled: Boolean = true,
    val shakeEnabled: Boolean = true,
    val shakeSensitivity: Float = 16.0f, // 12f (sensitive) to 26f (firm)
    val volumeUp2xEnabled: Boolean = false,
    val volumeUp3xEnabled: Boolean = true,
    val volumeDown2xEnabled: Boolean = false,
    val volumeDown3xEnabled: Boolean = true,
    val selectedAction: TriggerAction = TriggerAction.SILENT_MIC_RECORDING,
    val stealthNotification: StealthNotificationStyle = StealthNotificationStyle.BATTERY_SAVER,
    val hapticPulseOnStart: Boolean = true, // 2 subtle vibration pulses so user knows in pocket
    val maxRecordingDurationMinutes: Int = 5 // 0 for unlimited, 1, 3, 5, 10, 30
)
