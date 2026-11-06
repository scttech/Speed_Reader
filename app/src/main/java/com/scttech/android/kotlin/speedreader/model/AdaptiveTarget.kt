package com.scttech.android.kotlin.speedreader.model

import kotlin.math.roundToInt

/** Comprehension at or above this raises the next target; below [LOWER_COMPREHENSION_THRESHOLD] lowers it. */
const val RAISE_COMPREHENSION_THRESHOLD = 0.8f
const val LOWER_COMPREHENSION_THRESHOLD = 0.6f

/** First practice target = latest effective WPM + this percentage. */
const val INITIAL_TARGET_BOOST_PERCENT = 15
/** Used when the user has no sessions yet. */
const val DEFAULT_START_TARGET_WPM = 250
const val MIN_TARGET_WPM = 100
const val MAX_TARGET_WPM = MAX_PLAUSIBLE_RAW_WPM

/** Raise/lower step (percent) is user-configurable within this range. */
const val DEFAULT_ADJUST_PERCENT = 10
const val MIN_ADJUST_PERCENT = 1
const val MAX_ADJUST_PERCENT = 50

fun clampAdjustPercent(percent: Int): Int = percent.coerceIn(MIN_ADJUST_PERCENT, MAX_ADJUST_PERCENT)

enum class TargetChange { RAISED, LOWERED, HELD }

data class TargetAdjustment(val nextTargetWpm: Int, val change: TargetChange)

private fun roundToFive(value: Double): Int = (value / 5.0).roundToInt() * 5

private fun clampTarget(wpm: Int): Int = wpm.coerceIn(MIN_TARGET_WPM, MAX_TARGET_WPM)

fun initialTargetWpm(latestEffectiveWpm: Int): Int =
    if (latestEffectiveWpm <= 0) DEFAULT_START_TARGET_WPM
    else clampTarget(roundToFive(latestEffectiveWpm * (1 + INITIAL_TARGET_BOOST_PERCENT / 100.0)))

/**
 * Next practice target from the last result.
 * comprehension >= 80% -> raise by [raisePercent]; < 60% -> lower by [lowerPercent]; otherwise hold.
 */
fun nextTarget(
    currentTargetWpm: Int,
    comprehension: Float,
    raisePercent: Int,
    lowerPercent: Int,
): TargetAdjustment {
    val current = clampTarget(currentTargetWpm)
    val epsilon = 1e-4f
    val proposed = when {
        comprehension + epsilon >= RAISE_COMPREHENSION_THRESHOLD ->
            clampTarget(roundToFive(current * (1 + clampAdjustPercent(raisePercent) / 100.0)))
        comprehension + epsilon < LOWER_COMPREHENSION_THRESHOLD ->
            clampTarget(roundToFive(current * (1 - clampAdjustPercent(lowerPercent) / 100.0)))
        else -> current
    }
    val change = when {
        proposed > current -> TargetChange.RAISED
        proposed < current -> TargetChange.LOWERED
        else -> TargetChange.HELD
    }
    return TargetAdjustment(proposed, change)
}
