package com.scttech.android.kotlin.speedreader.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One completed reading activity (baseline test, practice read, ...). */
@Entity(tableName = "reading_sessions")
data class ReadingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    /** "baseline", "practice", "skim" ... */
    val type: String,
    val rawWpm: Int,
    /** 0f..1f */
    val comprehension: Float,
    val effectiveWpm: Int,
)
