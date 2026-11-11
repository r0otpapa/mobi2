package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TriggerType
import com.example.trigger.HardwareKeySequenceTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SilentGuard", appName)
    }

    @Test
    fun `test volume key triple press detection`() {
        var triggeredType: TriggerType? = null
        val tracker = HardwareKeySequenceTracker { trigger ->
            triggeredType = trigger
        }

        tracker.volumeUp3xEnabled = true

        // First press
        val res1 = tracker.onVolumeUpPressed()
        assertEquals(false, res1)
        assertEquals(null, triggeredType)

        // Second press
        val res2 = tracker.onVolumeUpPressed()
        assertEquals(false, res2)
        assertEquals(null, triggeredType)

        // Third press
        val res3 = tracker.onVolumeUpPressed()
        assertEquals(true, res3)
        assertEquals(TriggerType.VOLUME_UP_3X, triggeredType)
    }

    @Test
    fun `test volume down double press detection`() {
        var triggeredType: TriggerType? = null
        val tracker = HardwareKeySequenceTracker { trigger ->
            triggeredType = trigger
        }

        tracker.volumeDown2xEnabled = true
        tracker.volumeDown3xEnabled = false

        // First press
        val res1 = tracker.onVolumeDownPressed()
        assertEquals(false, res1)

        // Second press
        val res2 = tracker.onVolumeDownPressed()
        assertEquals(true, res2)
        assertEquals(TriggerType.VOLUME_DOWN_2X, triggeredType)
    }
}
