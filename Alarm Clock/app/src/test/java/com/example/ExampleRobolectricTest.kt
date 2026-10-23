package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AlarmEntity
import com.example.data.model.AmbientTheme
import com.example.data.model.ChallengeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AuraRise", appName)
    }

    @Test
    fun `test alarm formatting and repeat summary`() {
        val alarm = AlarmEntity(
            hour = 6,
            minute = 30,
            label = "Morning Rise",
            repeatDays = 31,
            challengeType = ChallengeType.MATH_PUZZLE
        )
        assertEquals("6:30 AM", alarm.formattedTime())
        assertEquals("Weekdays", alarm.daysSummary())
    }

    @Test
    fun `test arbitrary exact digit minutes and all-day themes`() {
        // Test arbitrary minute (not a multiple of 5: e.g. 14:23)
        val afternoonAlarm = AlarmEntity(
            hour = 14,
            minute = 23,
            label = "Afternoon Focus",
            ambientThemeName = "OCEAN_AURORA"
        )
        assertEquals("2:23 PM", afternoonAlarm.formattedTime())
        assertEquals("Afternoon", afternoonAlarm.timePeriodLabel())
        assertEquals(AmbientTheme.OCEAN_AURORA, afternoonAlarm.getAmbientTheme())
        assertTrue(afternoonAlarm.timeRemainingText().isNotEmpty())

        // Test night alarm with 47 minutes (not a multiple of 5)
        val nightAlarm = AlarmEntity(
            hour = 23,
            minute = 47,
            label = "Night Rest",
            ambientThemeName = "SUNSET_TWILIGHT"
        )
        assertEquals("11:47 PM", nightAlarm.formattedTime())
        assertEquals("Night", nightAlarm.timePeriodLabel())
        assertEquals(AmbientTheme.SUNSET_TWILIGHT, nightAlarm.getAmbientTheme())
    }
}
