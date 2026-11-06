package com.scttech.android.kotlin.speedreader

import com.scttech.android.kotlin.speedreader.model.DEFAULT_START_TARGET_WPM
import com.scttech.android.kotlin.speedreader.model.MAX_TARGET_WPM
import com.scttech.android.kotlin.speedreader.model.MIN_ADJUST_PERCENT
import com.scttech.android.kotlin.speedreader.model.MAX_ADJUST_PERCENT
import com.scttech.android.kotlin.speedreader.model.MIN_TARGET_WPM
import com.scttech.android.kotlin.speedreader.model.TargetChange
import com.scttech.android.kotlin.speedreader.model.clampAdjustPercent
import com.scttech.android.kotlin.speedreader.model.initialTargetWpm
import com.scttech.android.kotlin.speedreader.model.nextTarget
import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveTargetTest {

    @Test fun initialTargetIsLatestPlusFifteenPercentRoundedToFive() {
        assertEquals(230, initialTargetWpm(200))     // 230.0
        assertEquals(345, initialTargetWpm(300))     // 345.0
        assertEquals(290, initialTargetWpm(250))     // 287.5 -> 290
    }

    @Test fun initialTargetFallsBackWhenNoSessions() {
        assertEquals(DEFAULT_START_TARGET_WPM, initialTargetWpm(0))
        assertEquals(DEFAULT_START_TARGET_WPM, initialTargetWpm(-5))
    }

    @Test fun strongComprehensionRaisesByConfiguredPercent() {
        val r = nextTarget(400, comprehension = 0.8f, raisePercent = 10, lowerPercent = 10)
        assertEquals(440, r.nextTargetWpm); assertEquals(TargetChange.RAISED, r.change)
        val big = nextTarget(400, 1.0f, raisePercent = 25, lowerPercent = 10)
        assertEquals(500, big.nextTargetWpm)
    }

    @Test fun weakComprehensionLowersByConfiguredPercent() {
        val r = nextTarget(400, comprehension = 0.4f, raisePercent = 10, lowerPercent = 10)
        assertEquals(360, r.nextTargetWpm); assertEquals(TargetChange.LOWERED, r.change)
        val big = nextTarget(400, 0.2f, raisePercent = 10, lowerPercent = 30)
        assertEquals(280, big.nextTargetWpm)
    }

    @Test fun middleComprehensionHolds() {
        val r = nextTarget(400, comprehension = 0.6f, raisePercent = 10, lowerPercent = 10)
        assertEquals(400, r.nextTargetWpm); assertEquals(TargetChange.HELD, r.change)
        assertEquals(TargetChange.HELD, nextTarget(400, 0.79f, 10, 10).change)
    }

    @Test fun thresholdsAreInclusiveAtEightyAndExclusiveAtSixty() {
        assertEquals(TargetChange.RAISED, nextTarget(400, 4f / 5f, 10, 10).change)
        assertEquals(TargetChange.HELD, nextTarget(400, 3f / 5f, 10, 10).change)
        assertEquals(TargetChange.LOWERED, nextTarget(400, 2f / 5f, 10, 10).change)
    }

    @Test fun targetStaysWithinBounds() {
        val top = nextTarget(MAX_TARGET_WPM, 1f, 50, 10)
        assertEquals(MAX_TARGET_WPM, top.nextTargetWpm); assertEquals(TargetChange.HELD, top.change)
        val bottom = nextTarget(MIN_TARGET_WPM, 0f, 10, 50)
        assertEquals(MIN_TARGET_WPM, bottom.nextTargetWpm); assertEquals(TargetChange.HELD, bottom.change)
    }

    @Test fun adjustPercentIsClamped() {
        assertEquals(MIN_ADJUST_PERCENT, clampAdjustPercent(-4))
        assertEquals(MAX_ADJUST_PERCENT, clampAdjustPercent(500))
        // out-of-range settings values can never produce a zero or negative step
        assertEquals(405, nextTarget(400, 1f, raisePercent = 0, lowerPercent = 0).nextTargetWpm)
    }
}
