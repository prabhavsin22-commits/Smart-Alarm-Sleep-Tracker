package com.example.data

import android.content.Context
import com.example.data.model.SleepSessionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class HealthSyncStatus(
    val isConnected: Boolean = true,
    val sourceName: String = "Health Connect & Wearable HR",
    val lastSyncTimestamp: Long = System.currentTimeMillis() - 1800000,
    val nightRestingHrAverage: Int = 54,
    val baselineDaytimeHr: Int = 68,
    val nocturnalDipPercent: Float = 14.8f, // 10-20% is optimal dipping
    val dipClassification: String = "Normal Dipper (Optimal Recovery)",
    val isSyncing: Boolean = false
)

class HealthSyncRepository(private val context: Context) {

    private val _syncStatus = MutableStateFlow(HealthSyncStatus())
    val syncStatus: StateFlow<HealthSyncStatus> = _syncStatus.asStateFlow()

    suspend fun syncLatestNightData(): SleepSessionEntity {
        _syncStatus.value = _syncStatus.value.copy(isSyncing = true)
        // Simulate sync latency
        kotlinx.coroutines.delay(600)

        val now = System.currentTimeMillis()
        val durationMinutes = 7 * 60 + Random.nextInt(15, 55) // ~7.5 hours
        val startTime = now - (durationMinutes * 60 * 1000L)
        val cycles = durationMinutes / 90.0f

        val daytimeHr = 66 + Random.nextInt(-3, 5)
        val minHr = 48 + Random.nextInt(-2, 4)
        val maxHr = 72 + Random.nextInt(-2, 6)
        val avgHr = (minHr * 0.4 + maxHr * 0.2 + 55 * 0.4).toInt()
        val dip = ((daytimeHr - avgHr).toFloat() / daytimeHr) * 100f

        // Generate hourly HR curve over the night
        val hrPoints = mutableListOf<Int>()
        val hours = durationMinutes / 60
        for (i in 0..hours) {
            val progress = i.toFloat() / hours
            val sample = when {
                progress < 0.2f -> 64 - (progress * 50).toInt() // dropping as falling asleep
                progress < 0.6f -> minHr + Random.nextInt(0, 4) // deep sleep dip
                progress < 0.85f -> minHr + 4 + Random.nextInt(0, 5) // REM fluctuations
                else -> avgHr + 4 + Random.nextInt(0, 6) // morning cortisol waking rise
            }
            hrPoints.add(sample.coerceIn(minHr, maxHr))
        }

        val classification = when {
            dip >= 10f && dip <= 20f -> "Normal Dipper (Optimal Recovery)"
            dip > 20f -> "Extreme Dipper (Deep Restoration)"
            dip in 0.0f..10.0f -> "Non-Dipper (Elevated Night Stress)"
            else -> "Reverse Dipper"
        }

        val deepPercent = Random.nextInt(18, 25)
        val remPercent = Random.nextInt(20, 26)
        val awakePercent = Random.nextInt(4, 9)
        val lightPercent = 100 - deepPercent - remPercent - awakePercent

        val score = (82 + (dip.coerceIn(5f, 20f) * 0.8f).toInt() + Random.nextInt(-3, 4)).coerceIn(65, 98)

        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val session = SleepSessionEntity(
            dateString = dateFormat.format(Date(startTime)),
            startTimeMillis = startTime,
            endTimeMillis = now,
            durationMinutes = durationMinutes,
            sleepScore = score,
            cyclesCount = String.format(Locale.US, "%.1f", cycles).toFloat(),
            avgHeartRate = avgHr,
            minHeartRate = minHr,
            maxHeartRate = maxHr,
            hrDipPercent = String.format(Locale.US, "%.1f", dip).toFloat(),
            wakeReactionTimeSeconds = Random.nextInt(9, 28),
            challengeCompleted = "Math Puzzle (3/3 solved)",
            heartRatePoints = hrPoints.joinToString(","),
            deepSleepPercent = deepPercent,
            remSleepPercent = remPercent,
            lightSleepPercent = lightPercent,
            awakePercent = awakePercent
        )

        _syncStatus.value = HealthSyncStatus(
            isConnected = true,
            sourceName = "Health Connect & Wearable HR",
            lastSyncTimestamp = now,
            nightRestingHrAverage = avgHr,
            baselineDaytimeHr = daytimeHr,
            nocturnalDipPercent = String.format(Locale.US, "%.1f", dip).toFloat(),
            dipClassification = classification,
            isSyncing = false
        )

        return session
    }

    fun toggleHealthConnection(enable: Boolean) {
        _syncStatus.value = _syncStatus.value.copy(
            isConnected = enable,
            sourceName = if (enable) "Health Connect & Wearable HR" else "Manual Offline Log"
        )
    }
}
