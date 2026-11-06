package com.scttech.android.kotlin.speedreader.reminder

import android.content.Context
import com.scttech.android.kotlin.speedreader.data.DrillResultRepository
import com.scttech.android.kotlin.speedreader.data.SessionRepository
import com.scttech.android.kotlin.speedreader.data.SettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/** Lets the broadcast receivers reach the app's repositories (receivers can't use constructor injection). */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderEntryPoint {
    fun settings(): SettingsRepository
    fun sessions(): SessionRepository
    fun drills(): DrillResultRepository
    fun controller(): ReminderController

    companion object {
        fun from(context: Context): ReminderEntryPoint =
            EntryPointAccessors.fromApplication(context.applicationContext, ReminderEntryPoint::class.java)
    }
}
