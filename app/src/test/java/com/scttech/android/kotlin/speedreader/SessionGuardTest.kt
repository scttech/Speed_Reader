package com.scttech.android.kotlin.speedreader

import com.scttech.android.kotlin.speedreader.model.MAX_GOAL_WPM
import com.scttech.android.kotlin.speedreader.model.MAX_PLAUSIBLE_RAW_WPM
import com.scttech.android.kotlin.speedreader.model.MAX_TARGET_WPM
import com.scttech.android.kotlin.speedreader.model.isPlausibleRawWpm
import com.scttech.android.kotlin.speedreader.model.rawWpm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionGuardTest {
    @Test fun normalSpeedsAreAccepted() {
        assertTrue(isPlausibleRawWpm(250)); assertTrue(isPlausibleRawWpm(900))
        assertTrue(isPlausibleRawWpm(MAX_PLAUSIBLE_RAW_WPM))
    }

    @Test fun impossibleSpeedsAreRejected() {
        assertFalse(isPlausibleRawWpm(MAX_PLAUSIBLE_RAW_WPM + 1))
        assertFalse(isPlausibleRawWpm(2923))     // the rushed run seen on the emulator
        assertFalse(isPlausibleRawWpm(0)); assertFalse(isPlausibleRawWpm(-3))
    }

    @Test fun rushedReadOfAFullExcerptIsRejected() {
        // 1,572 words in 30 seconds
        assertFalse(isPlausibleRawWpm(rawWpm(1572, 30_000)))
        // the same excerpt in a realistic 5 minutes is fine
        assertTrue(isPlausibleRawWpm(rawWpm(1572, 300_000)))
    }

    @Test fun goalAndTargetMaximumsMatchTheGuard() {
        assertEquals(MAX_PLAUSIBLE_RAW_WPM, MAX_GOAL_WPM)
        assertEquals(MAX_PLAUSIBLE_RAW_WPM, MAX_TARGET_WPM)
    }
}
