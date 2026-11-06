package com.scttech.android.kotlin.speedreader.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uses an inexact, doze-friendly alarm (no special permission needed). The receiver reschedules itself each
 * day, and the app reschedules on launch and after a reboot.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReminderController {

    private val alarms get() = context.getSystemService(AlarmManager::class.java)

    private fun pendingIntent(): PendingIntent = PendingIntent.getBroadcast(
        context, REQUEST_CODE, Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    override fun schedule(minuteOfDay: Int) {
        val at = nextTriggerMillis(System.currentTimeMillis(), minuteOfDay, ZoneId.systemDefault())
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pendingIntent())
    }

    override fun cancel() {
        alarms.cancel(pendingIntent())
    }

    private companion object { const val REQUEST_CODE = 4107 }
}
