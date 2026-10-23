package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

enum class ChallengeType {
    NONE,
    MATH_PUZZLE,
    SKY_CAPTURE,
    MEMORY_SEQUENCE
}

enum class AmbientTheme(val displayName: String, val icon: String, val description: String) {
    SUNRISE_GOLD("Sunrise Gold", "☀️", "Warm amber to golden light (Morning Rise)"),
    OCEAN_AURORA("Ocean Aurora", "🌊", "Deep teal & soothing cyan (Afternoon / Calm)"),
    SUNSET_TWILIGHT("Sunset Twilight", "🌇", "Dusk rose & violet (Evening / Wind Down)"),
    CRISP_DAYLIGHT("Crisp Daylight", "💡", "Clean energizing white & amber (Work / Study)"),
    NEON_PULSE("Neon Pulse", "⚡", "High-visibility electric glow (Heavy Sleepers)"),
    AUDIO_ONLY("Audio & Haptics", "🔕", "Subtle sound & vibration without screen light")
}

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "Alarm",
    val isEnabled: Boolean = true,
    val repeatDays: Int = 31, // Bitmask: 1=Mon, 2=Tue, 4=Wed, 8=Thu, 16=Fri, 32=Sat, 64=Sun. 31 = Weekdays
    val soundName: String = "Forest Dawn Birds",
    val customSoundUri: String? = null,
    val sunriseDurationMinutes: Int = 10, // Gradual light ramp before alarm time
    val vibrate: Boolean = true,
    val challengeType: ChallengeType = ChallengeType.MATH_PUZZLE,
    val smartWakeCycle: Boolean = true, // Adjust within 20m window to wake in light sleep
    val ambientThemeName: String = "SUNRISE_GOLD"
) {
    fun isDaySelected(dayIndex: Int): Boolean { // 0 for Monday .. 6 for Sunday
        return (repeatDays and (1 shl dayIndex)) != 0
    }

    fun getAmbientTheme(): AmbientTheme {
        return try {
            AmbientTheme.valueOf(ambientThemeName)
        } catch (_: Exception) {
            AmbientTheme.SUNRISE_GOLD
        }
    }

    fun formattedTime(): String {
        val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val m = String.format("%02d", minute)
        val amPm = if (hour < 12) "AM" else "PM"
        return "$h:$m $amPm"
    }

    fun timePeriodLabel(): String {
        return when (hour) {
            in 5..11 -> "Morning"
            in 12..16 -> "Afternoon"
            in 17..21 -> "Evening"
            else -> "Night"
        }
    }

    fun daysSummary(): String {
        if (repeatDays == 127) return "Every day"
        if (repeatDays == 31) return "Weekdays"
        if (repeatDays == 96) return "Weekends"
        if (repeatDays == 0) return "Once"

        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val selected = mutableListOf<String>()
        for (i in 0..6) {
            if (isDaySelected(i)) {
                selected.add(days[i])
            }
        }
        return selected.joinToString(", ")
    }

    fun timeRemainingText(): String {
        val now = Calendar.getInstance()
        val alarmCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (alarmCal.timeInMillis <= now.timeInMillis) {
            alarmCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val diffMillis = alarmCal.timeInMillis - now.timeInMillis
        val diffMins = (diffMillis / 60000L).toInt()
        val hours = diffMins / 60
        val mins = diffMins % 60

        return when {
            hours == 0 && mins == 0 -> "Rings in less than a minute"
            hours == 0 -> "Rings in ${mins}m"
            mins == 0 -> "Rings in ${hours}h"
            else -> "Rings in ${hours}h ${mins}m"
        }
    }
}
