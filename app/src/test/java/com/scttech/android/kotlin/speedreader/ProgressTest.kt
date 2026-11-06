package com.scttech.android.kotlin.speedreader

import com.scttech.android.kotlin.speedreader.model.DEFAULT_GOAL_WPM
import com.scttech.android.kotlin.speedreader.model.MAX_GOAL_WPM
import com.scttech.android.kotlin.speedreader.model.MIN_GOAL_WPM
import com.scttech.android.kotlin.speedreader.model.clampGoal
import com.scttech.android.kotlin.speedreader.model.effectiveWpm
import com.scttech.android.kotlin.speedreader.model.gaugeLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressTest {
    @Test fun defaultGoalIs600() = assertEquals(600, DEFAULT_GOAL_WPM)

    @Test fun effectiveWpmScalesByComprehension() {
        assertEquals(240, effectiveWpm(300, 0.8f))
        assertEquals(0, effectiveWpm(300, -1f))
        assertEquals(300, effectiveWpm(300, 2f))
    }

    @Test fun gaugeLevelIsRelativeToGoalAndClamped() {
        assertEquals(0, gaugeLevel(0, 600))
        assertEquals(50, gaugeLevel(300, 600))
        assertEquals(100, gaugeLevel(600, 600))
        assertEquals(100, gaugeLevel(900, 600))
        assertEquals(50, gaugeLevel(500, 1000))
        assertEquals(0, gaugeLevel(300, 0))
    }

    @Test fun goalIsClamped() {
        assertEquals(MIN_GOAL_WPM, clampGoal(5))
        assertEquals(MAX_GOAL_WPM, clampGoal(99999))
        assertEquals(600, clampGoal(600))
    }
}
