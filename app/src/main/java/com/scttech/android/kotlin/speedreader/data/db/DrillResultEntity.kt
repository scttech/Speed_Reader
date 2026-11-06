package com.scttech.android.kotlin.speedreader.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Names stored in [DrillResultEntity.drill]. */
object DrillKind {
    const val SPAN = "span"
    const val COLUMN = "column"
    const val TWO_STOP = "twostop"
    const val METRONOME = "metronome"
    const val SCAN = "scan"
}

/**
 * One finished drill. [value] is the headline number for that drill:
 * span = widest phrase read (words), column/twostop = speed practised (WPM),
 * metronome = peak speed kept (WPM), scan = scan speed (WPM, 0 if the answer was wrong).
 * [detail] is a secondary number: span = correct rounds, column/twostop = seconds,
 * metronome = starting WPM, scan = 1 if correct else 0.
 */
@Entity(tableName = "drill_results")
data class DrillResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val drill: String,
    val value: Int,
    val detail: Int,
)
