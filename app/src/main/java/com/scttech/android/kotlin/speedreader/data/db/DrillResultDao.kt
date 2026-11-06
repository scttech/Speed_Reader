package com.scttech.android.kotlin.speedreader.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DrillResultDao {
    @Insert
    suspend fun insert(result: DrillResultEntity): Long

    @Query("SELECT * FROM drill_results ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<DrillResultEntity>>

    @Query("SELECT COUNT(*) FROM drill_results WHERE drill = :drill")
    suspend fun countOf(drill: String): Int
}
