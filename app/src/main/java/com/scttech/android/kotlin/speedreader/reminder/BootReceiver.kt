package com.scttech.android.kotlin.speedreader.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Alarms are cleared by a reboot, so book the reminder again if it is switched on. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val deps = ReminderEntryPoint.from(context)
        val settings = deps.settings(); val controller = deps.controller()
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if (settings.reminderEnabled.first()) controller.schedule(settings.reminderMinuteOfDay.first())
            } finally {
                pending.finish()
            }
        }
    }
}
