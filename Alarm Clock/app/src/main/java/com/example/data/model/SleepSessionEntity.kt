package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_sessions")
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMinutes: Int,
    val sleepScore: Int, // 0 - 100
    val cyclesCount: Float, // e.g. 5.1 cycles
    val avgHeartRate: Int, // e.g. 56 bpm
    val minHeartRate: Int, // e.g. 48 bpm
    val maxHeartRate: Int, // e.g. 72 bpm
    val hrDipPercent: Float, // Nocturnal dip vs daytime baseline, e.g. 14.2%
    val wakeReactionTimeSeconds: Int, // Alertness score: seconds to solve morning task
    val challengeCompleted: String,
    val heartRatePoints: String, // Comma-separated BPM samples across sleep
    val deepSleepPercent: Int,
    val remSleepPercent: Int,
    val lightSleepPercent: Int,
    val awakePercent: Int
) {
    fun formattedDuration(): String {
        val hours = durationMinutes / 60
        val mins = durationMinutes % 60
        return "${hours}h ${mins}m"
    }
}
