package com.scttech.android.kotlin.speedreader.model

import kotlin.math.roundToInt

/** Default goal, in effective words per minute (WPM x comprehension). */
const val DEFAULT_GOAL_WPM = 600
const val MIN_GOAL_WPM = 100
const val MAX_GOAL_WPM = MAX_PLAUSIBLE_RAW_WPM

fun clampGoal(wpm: Int): Int = wpm.coerceIn(MIN_GOAL_WPM, MAX_GOAL_WPM)

/** Effective WPM = raw WPM scaled by comprehension (0f..1f). */
fun effectiveWpm(rawWpm: Int, comprehension: Float): Int =
    (rawWpm * comprehension.coerceIn(0f, 1f)).roundToInt()

/** Maps effective WPM to the 0..100 "gauge" level relative to the user's goal. */
fun gaugeLevel(effectiveWpm: Int, goalWpm: Int): Int {
    if (goalWpm <= 0) return 0
    return (effectiveWpm * 100f / goalWpm).roundToInt().coerceIn(0, 100)
}
