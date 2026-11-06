package com.scttech.android.kotlin.speedreader

import com.scttech.android.kotlin.speedreader.data.PassageLibrary
import com.scttech.android.kotlin.speedreader.model.GuidedLine
import com.scttech.android.kotlin.speedreader.model.MAX_TARGET_WPM
import com.scttech.android.kotlin.speedreader.model.SPAN_MAX_WORDS
import com.scttech.android.kotlin.speedreader.model.SPAN_MIN_WORDS
import com.scttech.android.kotlin.speedreader.model.SpanLevel
import com.scttech.android.kotlin.speedreader.model.beatsOf
import com.scttech.android.kotlin.speedreader.model.columnLines
import com.scttech.android.kotlin.speedreader.model.corpusWords
import com.scttech.android.kotlin.speedreader.model.dwellMillis
import com.scttech.android.kotlin.speedreader.model.makeSpanQuestion
import com.scttech.android.kotlin.speedreader.model.nextSpanLevel
import com.scttech.android.kotlin.speedreader.model.rampWpm
import com.scttech.android.kotlin.speedreader.model.splitSentences
import com.scttech.android.kotlin.speedreader.model.twoStopLines
import com.scttech.android.kotlin.speedreader.model.wordClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DrillLogicTest {
    private val texts = PassageLibrary.baseline.map { it.text }
    private val sentences = splitSentences(texts)
    private val words = corpusWords(texts)

    // ---- span drill ----
    @Test fun twoCorrectInARowWidensByOne() {
        var l = SpanLevel(width = 3)
        l = nextSpanLevel(l, true); assertEquals(SpanLevel(3, 1), l)
        l = nextSpanLevel(l, true); assertEquals(SpanLevel(4, 0), l)
    }

    @Test fun missNarrowsAndResetsStreak() {
        assertEquals(SpanLevel(3, 0), nextSpanLevel(SpanLevel(4, 1), false))
    }

    @Test fun spanWidthStaysWithinBounds() {
        assertEquals(SPAN_MAX_WORDS, nextSpanLevel(SpanLevel(SPAN_MAX_WORDS, 1), true).width)
        assertEquals(SPAN_MIN_WORDS, nextSpanLevel(SpanLevel(SPAN_MIN_WORDS, 0), false).width)
    }

    @Test fun questionsAreWellFormedAtEveryWidth() {
        for (width in SPAN_MIN_WORDS..SPAN_MAX_WORDS) {
            repeat(60) { seed ->
                val q = makeSpanQuestion(sentences, width, Random(seed))
                assertEquals(4, q.options.size)
                assertEquals("options must be distinct", 4, q.options.toSet().size)
                assertEquals(q.phrase, q.options[q.correctIndex])
                assertTrue(q.options.all { it.split(" ").size == width })
            }
        }
    }

    @Test fun distractorsDifferFromThePhraseByOneWordMostly() {
        val q = makeSpanQuestion(sentences, 5, Random(7))
        val real = q.phrase.split(" ")
        val others = q.options.filterIndexed { i, _ -> i != q.correctIndex }
        assertTrue(others.all { o -> o.split(" ").zip(real).count { (a, b) -> a != b } >= 1 })
    }

    @Test fun wordClassesGroupSimilarWords() {
        assertEquals(wordClass("the"), wordClass("his"))
        assertEquals(wordClass("of"), wordClass("with"))
        assertEquals(wordClass("slowly"), wordClass("quickly"))
        assertEquals(wordClass("walking"), wordClass("running"))
        assertEquals(wordClass("watched"), wordClass("opened"))
        assertTrue(wordClass("the") != wordClass("colony"))
    }

    @Test fun distractorsChangeExactlyOneWordAndUsuallyKeepItsKind() {
        var total = 0; var sameClass = 0
        for (width in SPAN_MIN_WORDS..SPAN_MAX_WORDS) repeat(80) { seed ->
            val q = makeSpanQuestion(sentences, width, Random(seed))
            val real = q.phrase.split(" ")
            q.options.filterIndexed { i, _ -> i != q.correctIndex }.forEach { o ->
                val words = o.split(" ")
                val diffs = words.indices.filter { words[it] != real[it] }
                assertEquals("distractor must differ in one word: $o vs ${q.phrase}", 1, diffs.size)
                total++
                if (wordClass(words[diffs[0]]) == wordClass(real[diffs[0]])) sameClass++
            }
        }
        assertTrue("only $sameClass of $total swaps kept the word class", sameClass.toDouble() / total >= 0.85)
    }

    // ---- guided lines ----
    @Test fun columnLinesHoldThreeWordsAndOneStop() {
        val lines = columnLines(words)
        assertTrue(lines.dropLast(1).all { it.text.split(" ").size == 3 && it.chunks.size == 1 })
        assertEquals(words, lines.flatMap { it.text.split(" ") })
    }

    @Test fun twoStopLinesFitAndKeepEveryWordInOrder() {
        val lines = twoStopLines(words, maxChars = 30)
        assertTrue(lines.all { it.text.length <= 30 || it.text.split(" ").size == 1 })
        assertTrue(lines.all { it.chunks.size == 2 || it.text.split(" ").size < 2 })
        assertEquals(words, lines.flatMap { it.text.split(" ") })
    }

    @Test fun chunkRangesPointAtTheRightText() {
        val line = GuidedLine(listOf("the quick", "brown fox"))
        assertEquals("the quick", line.text.substring(line.chunkRange(0)))
        assertEquals("brown fox", line.text.substring(line.chunkRange(1)))
    }

    @Test fun beatsCountWordsPerChunk() {
        val beats = beatsOf(twoStopLines(words))
        assertEquals(words.size, beats.sumOf { it.words })
    }

    @Test fun dwellMatchesTargetSpeed() {
        assertEquals(720L, dwellMillis(words = 3, wpm = 250))
        assertEquals(60_000L, dwellMillis(words = 250, wpm = 250))
        assertEquals(60_000L * 3, dwellMillis(words = 3, wpm = 0 + 1))   // wpm floored at 1
    }

    // ---- metronome ramp ----
    @Test fun rampStepsUpEveryFifteenSeconds() {
        assertEquals(300, rampWpm(300, 0))
        assertEquals(300, rampWpm(300, 14_999))
        assertEquals(325, rampWpm(300, 15_000))
        assertEquals(375, rampWpm(300, 45_000))
    }

    @Test fun rampIsCapped() {
        assertEquals(MAX_TARGET_WPM, rampWpm(1490, 600_000))
    }
}
