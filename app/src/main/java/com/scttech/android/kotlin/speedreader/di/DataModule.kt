package com.scttech.android.kotlin.speedreader.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.scttech.android.kotlin.speedreader.data.DataStoreSettingsRepository
import com.scttech.android.kotlin.speedreader.data.DrillResultRepository
import com.scttech.android.kotlin.speedreader.data.LibraryPassageRepository
import com.scttech.android.kotlin.speedreader.data.PassageRepository
import com.scttech.android.kotlin.speedreader.data.RoomDrillResultRepository
import com.scttech.android.kotlin.speedreader.data.RoomSessionRepository
import com.scttech.android.kotlin.speedreader.data.SessionRepository
import com.scttech.android.kotlin.speedreader.data.SettingsRepository
import com.scttech.android.kotlin.speedreader.data.db.DrillResultDao
import com.scttech.android.kotlin.speedreader.data.db.MIGRATION_1_2
import com.scttech.android.kotlin.speedreader.data.db.ReadingSessionDao
import com.scttech.android.kotlin.speedreader.data.db.SpeedReaderDatabase
import com.scttech.android.kotlin.speedreader.reminder.ReminderController
import com.scttech.android.kotlin.speedreader.reminder.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object DataProvidesModule {
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.settingsDataStore

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SpeedReaderDatabase =
        Room.databaseBuilder(context, SpeedReaderDatabase::class.java, "speedreader.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideSessionDao(db: SpeedReaderDatabase): ReadingSessionDao = db.readingSessionDao()

    @Provides
    fun provideDrillResultDao(db: SpeedReaderDatabase): DrillResultDao = db.drillResultDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindsModule {
    @Binds
    abstract fun bindSettings(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds
    abstract fun bindSessions(impl: RoomSessionRepository): SessionRepository

    @Binds
    abstract fun bindDrillResults(impl: RoomDrillResultRepository): DrillResultRepository

    @Binds
    abstract fun bindReminder(impl: ReminderScheduler): ReminderController

    @Binds
    abstract fun bindPassages(impl: LibraryPassageRepository): PassageRepository
}
