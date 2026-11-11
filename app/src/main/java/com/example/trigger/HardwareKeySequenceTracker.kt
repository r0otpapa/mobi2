package com.example.trigger

import com.example.data.model.TriggerType

class HardwareKeySequenceTracker(
    private val onSequenceDetected: (TriggerType) -> Unit
) {
    private val volumeUpPresses = mutableListOf<Long>()
    private val volumeDownPresses = mutableListOf<Long>()

    private val windowMs = 1800L // 1.8 seconds time window for multi-presses

    var volumeUp2xEnabled: Boolean = false
    var volumeUp3xEnabled: Boolean = true
    var volumeDown2xEnabled: Boolean = false
    var volumeDown3xEnabled: Boolean = true

    @Synchronized
    fun onVolumeUpPressed(): Boolean {
        val now = System.currentTimeMillis()
        volumeUpPresses.removeAll { now - it > windowMs }
        volumeUpPresses.add(now)

        if (volumeUp3xEnabled && volumeUpPresses.size >= 3) {
            volumeUpPresses.clear()
            onSequenceDetected(TriggerType.VOLUME_UP_3X)
            return true
        }

        if (volumeUp2xEnabled && volumeUpPresses.size >= 2) {
            volumeUpPresses.clear()
            onSequenceDetected(TriggerType.VOLUME_UP_2X)
            return true
        }

        return false
    }

    @Synchronized
    fun onVolumeDownPressed(): Boolean {
        val now = System.currentTimeMillis()
        volumeDownPresses.removeAll { now - it > windowMs }
        volumeDownPresses.add(now)

        if (volumeDown3xEnabled && volumeDownPresses.size >= 3) {
            volumeDownPresses.clear()
            onSequenceDetected(TriggerType.VOLUME_DOWN_3X)
            return true
        }

        if (volumeDown2xEnabled && volumeDownPresses.size >= 2) {
            volumeDownPresses.clear()
            onSequenceDetected(TriggerType.VOLUME_DOWN_2X)
            return true
        }

        return false
    }

    fun reset() {
        volumeUpPresses.clear()
        volumeDownPresses.clear()
    }
}
