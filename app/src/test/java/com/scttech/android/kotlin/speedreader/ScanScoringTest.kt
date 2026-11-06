package com.scttech.android.kotlin.speedreader

import com.scttech.android.kotlin.speedreader.model.scanWpm
import org.junit.Assert.assertEquals
import org.junit.Test

class ScanScoringTest {
    @Test fun scanSpeedIsWordsPerMinuteWhenCorrect() {
        assertEquals(900, scanWpm(wordCount = 1500, seconds = 100, correct = true))
        assertEquals(1500, scanWpm(wordCount = 1500, seconds = 60, correct = true))
    }

    @Test fun wrongAnswerScoresNothing() = assertEquals(0, scanWpm(1500, 20, correct = false))

    @Test fun zeroSecondsIsTreatedAsOne() = assertEquals(90_000, scanWpm(1500, 0, correct = true))
}
