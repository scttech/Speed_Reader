package com.scttech.android.kotlin.speedreader.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingSessionDao {
    @Insert
    suspend fun insert(session: ReadingSessionEntity): Long

    @Query("SELECT * FROM reading_sessions ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<ReadingSessionEntity>>

    @Query("DELETE FROM reading_sessions")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM reading_sessions WHERE type = :type")
    suspend fun countOfType(type: String): Int

    @Query("SELECT * FROM reading_sessions ORDER BY timestampMillis DESC LIMIT 1")
    fun observeLatest(): Flow<ReadingSessionEntity?>
}
