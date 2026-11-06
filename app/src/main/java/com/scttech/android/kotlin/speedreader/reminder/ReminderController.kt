package com.scttech.android.kotlin.speedreader.reminder

/** Schedules or cancels the daily reminder alarm. An interface so the settings logic can be tested without Android. */
interface ReminderController {
    fun schedule(minuteOfDay: Int)
    fun cancel()
}
