package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.EmergencyRecording
import com.example.data.model.StealthNotificationStyle
import com.example.data.model.TriggerAction
import com.example.data.model.TriggerConfig
import com.example.data.model.TriggerType
import com.example.data.repository.RecordingRepository
import com.example.data.repository.SettingsRepository
import com.example.player.AudioPlayerManager
import com.example.player.PlaybackState
import com.example.service.EmergencyAccessibilityService
import com.example.service.EmergencyGuardService
import com.example.service.EmergencyRecorderService
import com.example.service.RecorderState
import com.example.trigger.EmergencyTriggerHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val selectedTab: Int = 0, // 0 = Guard / Dashboard, 1 = Trigger Settings, 2 = Recordings Vault
    val isTestSimulatorOpen: Boolean = false,
    val lastTriggerDetected: TriggerType? = null,
    val lastTriggerTimestamp: Long = 0,
    val isAccessibilityEnabled: Boolean = false,
    val isMicPermissionGranted: Boolean = false,
    val isNotificationPermissionGranted: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository.getInstance(application)
    private val recordingRepo = RecordingRepository.getInstance(application)
    val audioPlayer = AudioPlayerManager()

    val config: StateFlow<TriggerConfig> = settingsRepo.config
    val recorderState: StateFlow<RecorderState> = EmergencyRecorderService.recorderState
    val guardActive: StateFlow<Boolean> = EmergencyGuardService.guardActiveState
    val currentShakeG: StateFlow<Float> = EmergencyGuardService.currentShakeG
    val playbackState: StateFlow<PlaybackState> = audioPlayer.playbackState

    val recordings: StateFlow<List<EmergencyRecording>> = recordingRepo.allRecordings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        checkPermissionsAndServices()

        // Listen for live trigger events
        viewModelScope.launch {
            EmergencyTriggerHandler.triggerEvents.collect { (trigger, time) ->
                _uiState.value = _uiState.value.copy(
                    lastTriggerDetected = trigger,
                    lastTriggerTimestamp = time
                )
            }
        }
    }

    fun checkPermissionsAndServices() {
        val context = getApplication<Application>()
        val hasAccessibility = EmergencyAccessibilityService.isAccessibilityEnabled(context)
        _uiState.value = _uiState.value.copy(
            isAccessibilityEnabled = hasAccessibility
        )
    }

    fun setSelectedTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun setTestSimulatorOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isTestSimulatorOpen = open)
    }

    fun updatePermissionStatus(micGranted: Boolean, notifGranted: Boolean) {
        _uiState.value = _uiState.value.copy(
            isMicPermissionGranted = micGranted,
            isNotificationPermissionGranted = notifGranted
        )
    }

    fun toggleMasterGuard(enabled: Boolean) {
        settingsRepo.updateConfig { it.copy(isGuardEnabled = enabled) }
        val context = getApplication<Application>()
        if (enabled) {
            EmergencyGuardService.start(context)
        } else {
            EmergencyGuardService.stop(context)
        }
    }

    fun updateFlipEnabled(enabled: Boolean) {
        settingsRepo.updateConfig { it.copy(flipEnabled = enabled) }
    }

    fun updateShakeEnabled(enabled: Boolean) {
        settingsRepo.updateConfig { it.copy(shakeEnabled = enabled) }
    }

    fun updateShakeSensitivity(sensitivity: Float) {
        settingsRepo.updateConfig { it.copy(shakeSensitivity = sensitivity) }
    }

    fun updateVolumeUp2x(enabled: Boolean) {
        settingsRepo.updateConfig { it.copy(volumeUp2xEnabled = enabled) }
    }

    fun updateVolumeUp3x(enabled: Boolean) {
        settingsRepo.updateConfig { it.copy(volumeUp3xEnabled = enabled) }
    }

    fun updateVolumeDown2x(enabled: Boolean) {
        settingsRepo.updateConfig { it.copy(volumeDown2xEnabled = enabled) }
    }

    fun updateVolumeDown3x(enabled: Boolean) {
        settingsRepo.updateConfig { it.copy(volumeDown3xEnabled = enabled) }
    }

    fun updateTriggerAction(action: TriggerAction) {
        settingsRepo.updateConfig { it.copy(selectedAction = action) }
    }

    fun updateStealthNotification(style: StealthNotificationStyle) {
        settingsRepo.updateConfig { it.copy(stealthNotification = style) }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        settingsRepo.updateConfig { it.copy(hapticPulseOnStart = enabled) }
    }

    fun updateMaxDuration(minutes: Int) {
        settingsRepo.updateConfig { it.copy(maxRecordingDurationMinutes = minutes) }
    }

    // Recording Controls
    fun togglePanicRecording() {
        val context = getApplication<Application>()
        val intent = Intent(context, EmergencyRecorderService::class.java).apply {
            action = EmergencyRecorderService.ACTION_TOGGLE_RECORDING
            putExtra(EmergencyRecorderService.EXTRA_TRIGGER_SOURCE, "Manual Panic Button")
        }
        context.startService(intent)
    }

    fun stopRecording() {
        val context = getApplication<Application>()
        val intent = Intent(context, EmergencyRecorderService::class.java).apply {
            action = EmergencyRecorderService.ACTION_STOP_RECORDING
        }
        context.startService(intent)
    }

    // Playback Controls
    fun playRecording(recording: EmergencyRecording) {
        audioPlayer.play(recording.id, recording.filePath)
    }

    fun pausePlayback() {
        audioPlayer.pause()
    }

    fun seekPlayback(positionMs: Int) {
        audioPlayer.seekTo(positionMs)
    }

    fun stopPlayback() {
        audioPlayer.stop()
    }

    // Recording File Ops
    fun deleteRecording(recording: EmergencyRecording) {
        if (playbackState.value.currentRecordingId == recording.id) {
            audioPlayer.stop()
        }
        viewModelScope.launch {
            recordingRepo.deleteRecording(recording)
        }
    }

    fun updateRecordingNotes(recording: EmergencyRecording, newNotes: String) {
        viewModelScope.launch {
            recordingRepo.updateRecording(recording.copy(notes = newNotes))
        }
    }

    fun getShareUri(recording: EmergencyRecording): Uri? {
        return recordingRepo.getShareUri(recording)
    }

    fun testSimulateTrigger(triggerType: TriggerType) {
        val context = getApplication<Application>()
        EmergencyTriggerHandler.handleTrigger(context, triggerType)
    }

    fun openAccessibilitySettings() {
        EmergencyAccessibilityService.openAccessibilitySettings(getApplication())
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
