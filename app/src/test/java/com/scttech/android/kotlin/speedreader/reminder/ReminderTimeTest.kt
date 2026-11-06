package com.scttech.android.kotlin.speedreader.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderTimeTest {
    private val zone = ZoneId.of("America/Chicago")
    private fun at(h: Int, m: Int, day: Int = 10) = ZonedDateTime.of(2026, 3, day, h, m, 0, 0, zone).toInstant().toEpochMilli()

    @Test fun laterTodayWhenTimeNotYetReached() =
        assertEquals(at(19, 0), nextTriggerMillis(at(8, 0), 19 * 60, zone))

    @Test fun tomorrowWhenTimeHasPassed() =
        assertEquals(at(19, 0, 11), nextTriggerMillis(at(20, 0), 19 * 60, zone))

    @Test fun tomorrowWhenExactlyNow() =
        assertEquals(at(19, 0, 11), nextTriggerMillis(at(19, 0), 19 * 60, zone))

    @Test fun formatsTime() {
        assertEquals("7:00 PM", formatMinuteOfDay(19 * 60, java.util.Locale.US))
        assertEquals("12:05 AM", formatMinuteOfDay(5, java.util.Locale.US))
    }

    @Test fun remindsOnlyWhenEnabledAndNotYetPractised() {
        assertTrue(shouldRemind(enabled = true, practisedToday = false))
        assertFalse(shouldRemind(enabled = true, practisedToday = true))
        assertFalse(shouldRemind(enabled = false, practisedToday = false))
    }

    @Test fun messageMentionsStreak() {
        assertTrue(reminderMessage(5).contains("5-day"))
        assertFalse(reminderMessage(0).contains("streak"))
    }
}
