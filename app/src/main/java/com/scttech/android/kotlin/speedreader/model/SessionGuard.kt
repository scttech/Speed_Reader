package com.scttech.android.kotlin.speedreader.model

/**
 * Raw reading speeds above this are not real reads (tapping "Done" early / skimming),
 * so such attempts are not counted toward the gauge or the practice target.
 * Goal and target maximums match it, since effective WPM can never exceed raw WPM.
 */
const val MAX_PLAUSIBLE_RAW_WPM = 1500

fun isPlausibleRawWpm(rawWpm: Int): Boolean = rawWpm in 1..MAX_PLAUSIBLE_RAW_WPM
