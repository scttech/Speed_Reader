package com.scttech.android.kotlin.speedreader

import com.scttech.android.kotlin.speedreader.data.rotate
import com.scttech.android.kotlin.speedreader.data.PassageLibrary
import com.scttech.android.kotlin.speedreader.model.comprehension
import com.scttech.android.kotlin.speedreader.model.countWords
import com.scttech.android.kotlin.speedreader.model.rawWpm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PassageTest {
    private val passages = PassageLibrary.baseline

    @Test fun idsAreUnique() = assertEquals(passages.size, passages.map { it.id }.toSet().size)

    @Test fun passagesAreComparableLength() {
        passages.forEach { assertTrue("${it.id} has ${it.wordCount} words", it.wordCount in 330..470) }
        val counts = passages.map { it.wordCount }
        assertTrue("lengths too uneven: $counts", counts.max() - counts.min() <= 80)
    }

    @Test fun questionsAreWellFormed() {
        passages.forEach { p ->
            assertEquals("${p.id} question count", 5, p.questions.size)
            p.questions.forEach { q ->
                assertEquals("${p.id}: ${q.text}", 4, q.options.size)
                assertTrue(q.correctIndex in q.options.indices)
                assertEquals(q.options.size, q.options.toSet().size)
            }
        }
    }

    @Test fun correctAnswersAreNotAllInOnePosition() {
        val positions = passages.flatMap { p -> p.questions.map { it.correctIndex } }.toSet()
        assertEquals(setOf(0, 1, 2, 3), positions)
    }

    @Test fun baselineCorrectAnswersAreNotRecognisableByLength() {
        val all = passages.flatMap { it.questions }
        val longest = all.count { q ->
            val lens = q.options.map { it.length }
            lens[q.correctIndex] == lens.max() && lens.count { it == lens.max() } == 1
        }
        assertTrue("$longest of ${all.size} have the correct answer as the unique longest", longest.toDouble() / all.size <= 0.40)
    }

    @Test fun rotateWrapsAroundInBothDirections() {
        val ids = passages.map { it.id }
        assertEquals(ids[0], rotate(ids, 0))
        assertEquals(ids[1], rotate(ids, 1))
        assertEquals(ids[0], rotate(ids, ids.size))
        assertEquals(ids[0], rotate(ids, -ids.size))
        assertEquals(ids.last(), rotate(ids, -1))
    }

    @Test fun countWordsIgnoresExtraWhitespace() = assertEquals(4, countWords("  one two\n\nthree   four "))

    @Test fun rawWpmComputation() {
        assertEquals(300, rawWpm(300, 60_000))
        assertEquals(600, rawWpm(300, 30_000))
        assertEquals(150, rawWpm(300, 120_000))
        assertEquals(18_000, rawWpm(300, 0)) // elapsed floored to 1s
    }

    @Test fun comprehensionFraction() {
        assertEquals(0.6f, comprehension(3, 5), 0.0001f)
        assertEquals(0f, comprehension(0, 0), 0f)
    }
}
