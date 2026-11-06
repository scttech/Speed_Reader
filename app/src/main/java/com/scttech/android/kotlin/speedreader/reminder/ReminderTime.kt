package com.scttech.android.kotlin.speedreader.reminder

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

const val MINUTES_PER_DAY = 24 * 60
const val DEFAULT_REMINDER_MINUTE_OF_DAY = 19 * 60      // 7:00 pm

/** Epoch millis of the next occurrence of [minuteOfDay] strictly after [nowMillis] in [zone]. */
fun nextTriggerMillis(nowMillis: Long, minuteOfDay: Int, zone: ZoneId): Long {
    val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
    val time = LocalTime.of((minuteOfDay / 60).coerceIn(0, 23), (minuteOfDay % 60).coerceIn(0, 59))
    var next = now.toLocalDate().atTime(time).atZone(zone)
    if (!next.toInstant().isAfter(now.toInstant())) next = now.toLocalDate().plusDays(1).atTime(time).atZone(zone)
    return next.toInstant().toEpochMilli()
}

fun formatMinuteOfDay(minuteOfDay: Int, locale: Locale = Locale.getDefault()): String =
    LocalTime.of(minuteOfDay / 60, minuteOfDay % 60).format(DateTimeFormatter.ofPattern("h:mm a", locale))

/** The reminder is pointless if the user has already practised today. */
fun shouldRemind(enabled: Boolean, practisedToday: Boolean): Boolean = enabled && !practisedToday

fun reminderMessage(streakDays: Int): String =
    if (streakDays > 0) "Keep your $streakDays-day streak going — a few minutes of practice is all it takes."
    else "Time for your reading practice. A few minutes a day is how speed is built."
