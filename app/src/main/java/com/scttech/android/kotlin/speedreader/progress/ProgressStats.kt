package com.scttech.android.kotlin.speedreader.progress

import com.scttech.android.kotlin.speedreader.data.db.DrillKind
import com.scttech.android.kotlin.speedreader.data.db.DrillResultEntity
import com.scttech.android.kotlin.speedreader.data.db.ReadingSessionEntity
import com.scttech.android.kotlin.speedreader.model.gaugeLevel
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

/** How many of the most recent sessions the charts show. */
const val CHART_POINTS = 20
const val RECENT_ITEMS = 10

fun epochDay(millis: Long, zone: ZoneId): Long =
    Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().toEpochDay()

/** Consecutive active days ending today (or yesterday, so a streak survives until midnight). */
fun currentStreak(activeDays: Set<Long>, today: Long): Int {
    var day = if (today in activeDays) today else today - 1
    var n = 0
    while (day in activeDays) { n++; day-- }
    return n
}

fun bestStreak(activeDays: Set<Long>): Int {
    var best = 0; var run = 0; var prev: Long? = null
    for (d in activeDays.sorted()) {
        run = if (prev != null && d == prev + 1) run + 1 else 1
        best = maxOf(best, run); prev = d
    }
    return best
}

fun levelTitle(level: Int): String = when {
    level >= 100 -> "Checkered flag"
    level >= 75 -> "Redline"
    level >= 50 -> "Fast lane"
    level >= 25 -> "Street legal"
    else -> "Learner's permit"
}

data class DrillBest(val kind: String, val label: String, val best: String, val count: Int)

data class RecentItem(val timestampMillis: Long, val title: String, val detail: String)

data class ProgressSummary(
    val goalWpm: Int,
    val level: Int,
    val title: String,
    val latestEffectiveWpm: Int,
    val bestEffectiveWpm: Int,
    val recentAverageEffectiveWpm: Int,
    val sessionCount: Int,
    val streak: Int,
    val bestStreak: Int,
    /** Oldest to newest, at most [CHART_POINTS]. */
    val effectiveSeries: List<Int>,
    val comprehensionSeries: List<Int>,
    val drillBests: List<DrillBest>,
    val recent: List<RecentItem>,
) {
    val hasSessions: Boolean get() = sessionCount > 0
}

private fun drillLabel(kind: String) = when (kind) {
    DrillKind.SPAN -> "Span stretch"
    DrillKind.COLUMN -> "Column drill"
    DrillKind.TWO_STOP -> "Two-stop lines"
    DrillKind.METRONOME -> "Metronome ramp"
    DrillKind.SCAN -> "Skim & scan"
    else -> kind
}

private fun drillBestText(kind: String, rows: List<DrillResultEntity>): String = when (kind) {
    DrillKind.SPAN -> "${rows.maxOf { it.value }} words at a glance"
    DrillKind.SCAN -> {
        val found = rows.count { it.detail == 1 }
        "${rows.maxOf { it.value }} WPM · ${found * 100 / rows.size}% found"
    }
    else -> "${rows.maxOf { it.value }} WPM"
}

private fun drillDetailText(r: DrillResultEntity) = when (r.drill) {
    DrillKind.SPAN -> "Widest phrase: ${r.value} words"
    DrillKind.METRONOME -> "Kept pace up to ${r.value} WPM"
    DrillKind.SCAN -> if (r.detail == 1) "Found it · ${r.value} WPM" else "Missed the answer"
    else -> "${r.value} WPM · ${r.detail}s"
}

fun buildProgress(
    sessions: List<ReadingSessionEntity>,
    drills: List<DrillResultEntity>,
    goalWpm: Int,
    today: Long,
    zone: ZoneId,
): ProgressSummary {
    val chronological = sessions.sortedBy { it.timestampMillis }
    val latest = chronological.lastOrNull()?.effectiveWpm ?: 0
    val recentAvg = chronological.takeLast(5).map { it.effectiveWpm }.average().let { if (it.isNaN()) 0 else it.roundToInt() }
    val level = gaugeLevel(latest, goalWpm)
    val days = (sessions.map { epochDay(it.timestampMillis, zone) } + drills.map { epochDay(it.timestampMillis, zone) }).toSet()

    val bests = listOf(DrillKind.SPAN, DrillKind.COLUMN, DrillKind.TWO_STOP, DrillKind.METRONOME, DrillKind.SCAN).mapNotNull { kind ->
        val rows = drills.filter { it.drill == kind }
        if (rows.isEmpty()) null else DrillBest(kind, drillLabel(kind), drillBestText(kind, rows), rows.size)
    }

    val recent = (
        sessions.map {
            RecentItem(
                it.timestampMillis,
                if (it.type == "baseline") "Baseline test" else "Practice reading",
                "${it.effectiveWpm} effective WPM · ${(it.comprehension * 100).roundToInt()}% comprehension",
            )
        } + drills.map { RecentItem(it.timestampMillis, drillLabel(it.drill), drillDetailText(it)) }
        ).sortedByDescending { it.timestampMillis }.take(RECENT_ITEMS)

    return ProgressSummary(
        goalWpm = goalWpm,
        level = level,
        title = levelTitle(level),
        latestEffectiveWpm = latest,
        bestEffectiveWpm = chronological.maxOfOrNull { it.effectiveWpm } ?: 0,
        recentAverageEffectiveWpm = recentAvg,
        sessionCount = sessions.size,
        streak = currentStreak(days, today),
        bestStreak = bestStreak(days),
        effectiveSeries = chronological.takeLast(CHART_POINTS).map { it.effectiveWpm },
        comprehensionSeries = chronological.takeLast(CHART_POINTS).map { (it.comprehension * 100).roundToInt() },
        drillBests = bests,
        recent = recent,
    )
}
