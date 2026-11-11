package com.example.trigger

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.data.model.TriggerType
import kotlin.math.abs
import kotlin.math.sqrt

class TriggerDetector(
    private val onTrigger: (TriggerType) -> Unit,
    private val onShakeProgress: ((currentG: Float) -> Unit)? = null
) : SensorEventListener {

    // Configuration
    var isFlipEnabled: Boolean = true
    var isShakeEnabled: Boolean = true
    var shakeThreshold: Float = 16.0f // m/s^2 delta

    // Internal state
    private var lastShakeTimestamp: Long = 0
    private var lastFlipTimestamp: Long = 0
    private var wasFaceUpOrUpright: Boolean = true
    private var faceDownStartTime: Long = 0

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                handleAccelerometer(event)
            }
            Sensor.TYPE_GRAVITY -> {
                handleGravity(event)
            }
        }
    }

    private fun handleAccelerometer(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val currentTime = System.currentTimeMillis()

        // 1. Shake Detection
        if (isShakeEnabled) {
            val gForce = sqrt(x * x + y * y + z * z)
            val deltaG = abs(gForce - SensorManager.GRAVITY_EARTH)

            onShakeProgress?.invoke(deltaG)

            if (deltaG > shakeThreshold) {
                if (currentTime - lastShakeTimestamp > 2500L) {
                    lastShakeTimestamp = currentTime
                    onTrigger(TriggerType.SHAKE)
                }
            }
        }

        // 2. Flip Face-Down Detection (z axis negative when face down)
        if (isFlipEnabled) {
            // If z > 2.0 or y > 5.0, phone is face up or in normal hand orientation
            if (z > 2.0f || abs(y) > 4.0f) {
                wasFaceUpOrUpright = true
                faceDownStartTime = 0
            } else if (z < -7.0f) {
                // Phone is flat face down (gravity pulling down on the screen side)
                if (wasFaceUpOrUpright) {
                    if (faceDownStartTime == 0L) {
                        faceDownStartTime = currentTime
                    } else if (currentTime - faceDownStartTime > 400L) { // Must stay face down for > 400ms to confirm intentional flip
                        if (currentTime - lastFlipTimestamp > 3000L) {
                            lastFlipTimestamp = currentTime
                            wasFaceUpOrUpright = false
                            onTrigger(TriggerType.FLIP)
                        }
                    }
                }
            }
        }
    }

    private fun handleGravity(event: SensorEvent) {
        val z = event.values[2]
        val currentTime = System.currentTimeMillis()

        if (isFlipEnabled) {
            if (z > 2.0f) {
                wasFaceUpOrUpright = true
                faceDownStartTime = 0
            } else if (z < -7.0f && wasFaceUpOrUpright) {
                if (faceDownStartTime == 0L) {
                    faceDownStartTime = currentTime
                } else if (currentTime - faceDownStartTime > 400L) {
                    if (currentTime - lastFlipTimestamp > 3000L) {
                        lastFlipTimestamp = currentTime
                        wasFaceUpOrUpright = false
                        onTrigger(TriggerType.FLIP)
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
