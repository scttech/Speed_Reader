package com.scttech.android.kotlin.speedreader.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.scttech.android.kotlin.speedreader.progress.currentStreak
import com.scttech.android.kotlin.speedreader.progress.epochDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/** Fires at the reminder time: posts the notification (unless already practised today) and books tomorrow's. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val deps = ReminderEntryPoint.from(context)
        val settings = deps.settings(); val sessions = deps.sessions(); val drills = deps.drills(); val controller = deps.controller()
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val enabled = settings.reminderEnabled.first()
                if (!enabled) return@launch
                val zone = ZoneId.systemDefault()
                val today = LocalDate.now(zone).toEpochDay()
                val days = (sessions.sessions.first().map { epochDay(it.timestampMillis, zone) } +
                    drills.results.first().map { epochDay(it.timestampMillis, zone) }).toSet()
                if (shouldRemind(enabled, practisedToday = today in days)) {
                    ReminderNotifier.show(context, currentStreak(days, today))
                }
                controller.schedule(settings.reminderMinuteOfDay.first())    // tomorrow's
            } finally {
                pending.finish()
            }
        }
    }
}
