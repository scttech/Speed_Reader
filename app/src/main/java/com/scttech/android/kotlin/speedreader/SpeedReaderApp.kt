package com.scttech.android.kotlin.speedreader

import android.app.Application
import com.scttech.android.kotlin.speedreader.data.SettingsRepository
import com.scttech.android.kotlin.speedreader.reminder.ReminderController
import com.scttech.android.kotlin.speedreader.reminder.ReminderNotifier
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SpeedReaderApp : Application() {
    @Inject lateinit var settings: SettingsRepository
    @Inject lateinit var reminder: ReminderController

    override fun onCreate() {
        super.onCreate()
        ReminderNotifier.createChannel(this)
        // Re-book the daily reminder on every launch, in case the system dropped the alarm.
        CoroutineScope(Dispatchers.Default).launch {
            if (settings.reminderEnabled.first()) reminder.schedule(settings.reminderMinuteOfDay.first())
        }
    }
}
