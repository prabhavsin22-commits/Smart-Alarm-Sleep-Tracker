package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SleepSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_sessions ORDER BY endTimeMillis DESC")
    fun getAllSessions(): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions ORDER BY endTimeMillis DESC LIMIT 1")
    fun getLatestSession(): Flow<SleepSessionEntity?>

    @Query("SELECT * FROM sleep_sessions ORDER BY endTimeMillis DESC LIMIT 7")
    fun getLastWeekSessions(): Flow<List<SleepSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SleepSessionEntity): Long

    @Query("SELECT COUNT(*) FROM sleep_sessions")
    suspend fun getSessionCount(): Int
}
