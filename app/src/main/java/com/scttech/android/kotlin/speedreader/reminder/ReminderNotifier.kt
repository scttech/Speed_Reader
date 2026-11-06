package com.scttech.android.kotlin.speedreader.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.scttech.android.kotlin.speedreader.MainActivity
import com.scttech.android.kotlin.speedreader.R

object ReminderNotifier {
    private const val CHANNEL_ID = "daily_reminder"
    private const val NOTIFICATION_ID = 4108

    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, "Daily reminder", NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "A once-a-day nudge to practise reading" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun show(context: Context, streakDays: Int) {
        if (!hasPermission(context)) return
        createChannel(context)
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle("Speed Reader")
            .setContentText(reminderMessage(streakDays))
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminderMessage(streakDays)))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, n)
    }
}
