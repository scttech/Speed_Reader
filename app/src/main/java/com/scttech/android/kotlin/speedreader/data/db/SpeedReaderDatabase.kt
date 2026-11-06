package com.scttech.android.kotlin.speedreader.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ReadingSessionEntity::class, DrillResultEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class SpeedReaderDatabase : RoomDatabase() {
    abstract fun readingSessionDao(): ReadingSessionDao
    abstract fun drillResultDao(): DrillResultDao
}

/** v1 -> v2: adds the drill_results table; existing reading sessions are untouched. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `drill_results` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`timestampMillis` INTEGER NOT NULL, " +
                "`drill` TEXT NOT NULL, " +
                "`value` INTEGER NOT NULL, " +
                "`detail` INTEGER NOT NULL)",
        )
    }
}
