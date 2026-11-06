package com.scttech.android.kotlin.speedreader

import com.scttech.android.kotlin.speedreader.data.db.DrillKind
import com.scttech.android.kotlin.speedreader.data.db.DrillResultEntity
import com.scttech.android.kotlin.speedreader.data.db.ReadingSessionEntity
import com.scttech.android.kotlin.speedreader.progress.CHART_POINTS
import com.scttech.android.kotlin.speedreader.progress.bestStreak
import com.scttech.android.kotlin.speedreader.progress.buildProgress
import com.scttech.android.kotlin.speedreader.progress.currentStreak
import com.scttech.android.kotlin.speedreader.progress.epochDay
import com.scttech.android.kotlin.speedreader.progress.levelTitle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class ProgressStatsTest {
    private val zone: ZoneId = ZoneOffset.UTC
    private val day0 = LocalDate.of(2026, 11, 1).toEpochDay()
    private fun millis(day: Long, hour: Int = 12) = (day * 24 + hour) * 3_600_000L

    private fun session(day: Long, eff: Int, comp: Float = 0.8f, type: String = "practice", hour: Int = 12) =
        ReadingSessionEntity(timestampMillis = millis(day, hour), type = type, rawWpm = 300, comprehension = comp, effectiveWpm = eff)

    @Test fun streakCountsConsecutiveDaysEndingTodayOrYesterday() {
        val days = setOf(day0, day0 + 1, day0 + 2)
        assertEquals(3, currentStreak(days, today = day0 + 2))
        assertEquals(3, currentStreak(days, today = day0 + 3))   // yesterday still counts
        assertEquals(0, currentStreak(days, today = day0 + 4))   // streak broken
        assertEquals(0, currentStreak(emptySet(), today = day0))
    }

    @Test fun bestStreakFindsLongestRun() {
        assertEquals(3, bestStreak(setOf(day0, day0 + 1, day0 + 2, day0 + 5, day0 + 6)))
        assertEquals(1, bestStreak(setOf(day0, day0 + 2, day0 + 4)))
        assertEquals(0, bestStreak(emptySet()))
    }

    @Test fun levelTitlesFollowTheGauge() {
        assertEquals("Learner's permit", levelTitle(0)); assertEquals("Learner's permit", levelTitle(24))
        assertEquals("Street legal", levelTitle(25)); assertEquals("Fast lane", levelTitle(50))
        assertEquals("Redline", levelTitle(75)); assertEquals("Checkered flag", levelTitle(100))
    }

    @Test fun epochDayUsesTheGivenZone() {
        val lateUtc = millis(day0, hour = 23)
        assertEquals(day0, epochDay(lateUtc, ZoneOffset.UTC))
        assertEquals(day0 + 1, epochDay(lateUtc, ZoneOffset.ofHours(5)))
    }

    @Test fun emptyHistoryGivesZeros() {
        val p = buildProgress(emptyList(), emptyList(), 600, today = day0, zone = zone)
        assertFalse(p.hasSessions); assertEquals(0, p.level); assertEquals(0, p.streak)
        assertTrue(p.effectiveSeries.isEmpty()); assertTrue(p.recent.isEmpty()); assertTrue(p.drillBests.isEmpty())
    }

    @Test fun summaryUsesLatestSessionForLevelAndBestForBest() {
        val s = listOf(session(day0, 300), session(day0 + 1, 450), session(day0 + 2, 360))
        val p = buildProgress(s.shuffled(), emptyList(), 600, today = day0 + 2, zone = zone)
        assertEquals(360, p.latestEffectiveWpm); assertEquals(450, p.bestEffectiveWpm)
        assertEquals(60, p.level)                                 // 360 / 600
        assertEquals("Fast lane", p.title)
        assertEquals(370, p.recentAverageEffectiveWpm)            // (300+450+360)/3
        assertEquals(listOf(300, 450, 360), p.effectiveSeries)    // oldest to newest
        assertEquals(3, p.streak)
    }

    @Test fun chartsShowOnlyTheMostRecentSessions() {
        val s = (0 until 30).map { session(day0 + it, 100 + it) }
        val p = buildProgress(s, emptyList(), 600, today = day0 + 29, zone = zone)
        assertEquals(CHART_POINTS, p.effectiveSeries.size)
        assertEquals(110, p.effectiveSeries.first()); assertEquals(129, p.effectiveSeries.last())
    }

    @Test fun drillsCountTowardStreakAndShowBests() {
        val drills = listOf(
            DrillResultEntity(1, millis(day0 + 1), DrillKind.SPAN, value = 5, detail = 9),
            DrillResultEntity(2, millis(day0 + 1, 15), DrillKind.SPAN, value = 7, detail = 10),
            DrillResultEntity(3, millis(day0 + 1, 16), DrillKind.SCAN, value = 800, detail = 1),
            DrillResultEntity(4, millis(day0 + 1, 17), DrillKind.SCAN, value = 0, detail = 0),
            DrillResultEntity(5, millis(day0 + 1, 18), DrillKind.METRONOME, value = 325, detail = 250),
        )
        val p = buildProgress(listOf(session(day0, 300)), drills, 600, today = day0 + 1, zone = zone)
        assertEquals(2, p.streak)
        assertEquals(listOf("Span stretch", "Metronome ramp", "Skim & scan"), p.drillBests.map { it.label })
        assertEquals("7 words at a glance", p.drillBests.first { it.kind == DrillKind.SPAN }.best)
        assertEquals("800 WPM · 50% found", p.drillBests.first { it.kind == DrillKind.SCAN }.best)
        assertEquals(2, p.drillBests.first { it.kind == DrillKind.SPAN }.count)
    }

    @Test fun recentActivityMixesSessionsAndDrillsNewestFirstAndIsCapped() {
        val s = (0 until 8).map { session(day0 + it, 300) }
        val d = (0 until 8).map { DrillResultEntity(it + 1L, millis(day0 + it, 18), DrillKind.SPAN, 4, 8) }
        val p = buildProgress(s, d, 600, today = day0 + 7, zone = zone)
        assertEquals(10, p.recent.size)
        assertTrue(p.recent.zipWithNext().all { (a, b) -> a.timestampMillis >= b.timestampMillis })
        assertEquals("Span stretch", p.recent.first().title)
    }
}
