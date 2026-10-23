package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AlarmDao
import com.example.data.dao.SleepDao
import com.example.data.model.AlarmEntity
import com.example.data.model.ChallengeType
import com.example.data.model.SleepSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [AlarmEntity::class, SleepSessionEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AuraRiseDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun sleepDao(): SleepDao

    companion object {
        @Volatile
        private var INSTANCE: AuraRiseDatabase? = null

        fun getDatabase(context: Context): AuraRiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuraRiseDatabase::class.java,
                    "aurora_alarm_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    populateInitialData(database)
                                }
                            }
                        }

                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    populateInitialData(database)
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: AuraRiseDatabase) {
            val alarmDao = database.alarmDao()
            val sleepDao = database.sleepDao()

            // Default All-Day Alarms (Morning, Afternoon, Evening)
            alarmDao.insertAlarm(
                AlarmEntity(
                    hour = 7,
                    minute = 15,
                    label = "Morning Wake Up",
                    isEnabled = true,
                    repeatDays = 31, // Mon-Fri
                    soundName = "Forest Dawn Birds",
                    sunriseDurationMinutes = 15,
                    vibrate = true,
                    challengeType = ChallengeType.MATH_PUZZLE,
                    smartWakeCycle = true,
                    ambientThemeName = "SUNRISE_GOLD"
                )
            )
            alarmDao.insertAlarm(
                AlarmEntity(
                    hour = 14,
                    minute = 45,
                    label = "Afternoon Focus Break",
                    isEnabled = false,
                    repeatDays = 31, // Mon-Fri
                    soundName = "Ocean Horizon Waves",
                    sunriseDurationMinutes = 10,
                    vibrate = true,
                    challengeType = ChallengeType.NONE,
                    smartWakeCycle = false,
                    ambientThemeName = "OCEAN_AURORA"
                )
            )
            alarmDao.insertAlarm(
                AlarmEntity(
                    hour = 20,
                    minute = 30,
                    label = "Evening Wind Down",
                    isEnabled = false,
                    repeatDays = 127, // Every day
                    soundName = "Tibetan Singing Bowl",
                    sunriseDurationMinutes = 10,
                    vibrate = true,
                    challengeType = ChallengeType.NONE,
                    smartWakeCycle = false,
                    ambientThemeName = "SUNSET_TWILIGHT"
                )
            )

            // Historical Sleep Sessions with nocturnal Heart Rate dip
            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
            val now = System.currentTimeMillis()
            val dayMillis = 86400000L

            val pastSessions = listOf(
                SleepSessionEntity(
                    dateString = dateFormat.format(Date(now - dayMillis)),
                    startTimeMillis = now - dayMillis - (7 * 3600000L + 40 * 60000L),
                    endTimeMillis = now - dayMillis,
                    durationMinutes = 460, // 7h 40m
                    sleepScore = 91,
                    cyclesCount = 5.1f,
                    avgHeartRate = 54,
                    minHeartRate = 48,
                    maxHeartRate = 68,
                    hrDipPercent = 15.6f,
                    wakeReactionTimeSeconds = 14,
                    challengeCompleted = "Math Challenge (14s)",
                    heartRatePoints = "66,62,56,51,49,48,52,57,63",
                    deepSleepPercent = 23,
                    remSleepPercent = 24,
                    lightSleepPercent = 48,
                    awakePercent = 5
                ),
                SleepSessionEntity(
                    dateString = dateFormat.format(Date(now - 2 * dayMillis)),
                    startTimeMillis = now - 2 * dayMillis - (7 * 3600000L + 15 * 60000L),
                    endTimeMillis = now - 2 * dayMillis,
                    durationMinutes = 435, // 7h 15m
                    sleepScore = 87,
                    cyclesCount = 4.8f,
                    avgHeartRate = 56,
                    minHeartRate = 50,
                    maxHeartRate = 70,
                    hrDipPercent = 14.1f,
                    wakeReactionTimeSeconds = 19,
                    challengeCompleted = "Sky Photo (Verified)",
                    heartRatePoints = "67,61,57,53,50,52,54,58,64",
                    deepSleepPercent = 20,
                    remSleepPercent = 22,
                    lightSleepPercent = 51,
                    awakePercent = 7
                ),
                SleepSessionEntity(
                    dateString = dateFormat.format(Date(now - 3 * dayMillis)),
                    startTimeMillis = now - 3 * dayMillis - (8 * 3600000L),
                    endTimeMillis = now - 3 * dayMillis,
                    durationMinutes = 480, // 8h
                    sleepScore = 94,
                    cyclesCount = 5.3f,
                    avgHeartRate = 52,
                    minHeartRate = 47,
                    maxHeartRate = 66,
                    hrDipPercent = 17.2f,
                    wakeReactionTimeSeconds = 11,
                    challengeCompleted = "Math Challenge (11s)",
                    heartRatePoints = "65,59,54,49,47,48,50,55,61",
                    deepSleepPercent = 25,
                    remSleepPercent = 25,
                    lightSleepPercent = 45,
                    awakePercent = 5
                )
            )

            for (session in pastSessions) {
                sleepDao.insertSession(session)
            }
        }
    }
}
