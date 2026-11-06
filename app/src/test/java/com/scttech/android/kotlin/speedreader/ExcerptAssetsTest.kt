package com.scttech.android.kotlin.speedreader

import com.scttech.android.kotlin.speedreader.data.ExcerptParser
import com.scttech.android.kotlin.speedreader.model.PassageKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Validates the generated practice excerpts under src/main/assets/excerpts. */
class ExcerptAssetsTest {
    private val excerpts by lazy {
        File("src/main/assets/excerpts").listFiles { f -> f.name.endsWith(".json") }!!
            .sortedBy { it.name }.map { ExcerptParser.parse(it.readText()) }
    }

    @Test fun thereAreSixtySixExcerpts() = assertEquals(66, excerpts.size)

    @Test fun idsAreUnique() = assertEquals(66, excerpts.map { it.id }.toSet().size)

    @Test fun lengthsAreWithinRange() {
        excerpts.forEach { assertTrue("${it.id} has ${it.wordCount} words", it.wordCount in 1200..1900) }
    }

    @Test fun fictionAndNonFictionAreBothPresent() {
        val fiction = excerpts.count { it.kind == PassageKind.FICTION }
        val nonfiction = excerpts.count { it.kind == PassageKind.NONFICTION }
        assertEquals(66, fiction + nonfiction)
        assertTrue("fiction=$fiction", fiction >= 30)
        assertTrue("nonfiction=$nonfiction", nonfiction >= 20)
    }

    @Test fun orderAlternatesAtTheStart() {
        val firstTen = excerpts.take(10).map { it.kind }
        firstTen.zipWithNext().forEach { (a, b) -> assertTrue("kinds should alternate: $firstTen", a != b) }
    }

    @Test fun everyExcerptHasFiveWellFormedQuestions() {
        excerpts.forEach { p ->
            assertEquals("${p.id} question count", 5, p.questions.size)
            p.questions.forEach { q ->
                assertEquals("${p.id}: ${q.text}", 4, q.options.size)
                assertEquals(4, q.options.toSet().size)
                assertTrue(q.correctIndex in 0..3)
            }
        }
    }

    @Test fun correctAnswersAreSpreadAcrossPositions() {
        val counts = excerpts.flatMap { p -> p.questions.map { it.correctIndex } }.groupingBy { it }.eachCount()
        val total = excerpts.sumOf { it.questions.size }
        (0..3).forEach { assertTrue("position $it used ${counts[it]} of $total", (counts[it] ?: 0) in (total * 22 / 100)..(total * 28 / 100)) }
    }

    @Test fun correctAnswerIsNotRecognisableByLength() {
        // If the right answer is usually the longest option, readers can score without reading.
        val all = excerpts.flatMap { it.questions }
        val longest = all.count { q ->
            val lens = q.options.map { it.length }
            lens[q.correctIndex] == lens.max() && lens.count { it == lens.max() } == 1
        }
        val share = longest.toDouble() / all.size
        assertTrue("correct answer is the unique longest in ${(share * 100).toInt()}% of questions (chance is 25%)", share <= 0.40)
    }

    @Test fun everyExcerptHasAtLeastFourScanQuestions() {
        excerpts.forEach { assertTrue("${it.id} has ${it.scanQuestions.size} scan questions", it.scanQuestions.size >= 4) }
    }

    @Test fun scanQuestionsAreWellFormedAndPointAtRealText() {
        excerpts.forEach { p ->
            p.scanQuestions.forEach { sq ->
                val q = sq.question
                assertEquals("${p.id}: ${q.text}", 4, q.options.size)
                assertEquals(4, q.options.toSet().size)
                assertTrue(q.correctIndex in 0..3)
                assertTrue("${p.id}: evidence is not in the text: ${sq.evidence.take(60)}", p.text.contains(sq.evidence))
                assertTrue("${p.id}: evidence is too long to highlight usefully", sq.evidence.length in 10..900)
            }
        }
    }

    @Test fun scanAnswersAreSpreadAndNotRecognisableByLength() {
        val all = excerpts.flatMap { it.scanQuestions }.map { it.question }
        val counts = all.groupingBy { it.correctIndex }.eachCount()
        (0..3).forEach { assertTrue("scan position $it used ${counts[it]} of ${all.size}", (counts[it] ?: 0) in (all.size * 22 / 100)..(all.size * 28 / 100)) }
        val longest = all.count { q ->
            val lens = q.options.map { it.length }
            lens[q.correctIndex] == lens.max() && lens.count { it == lens.max() } == 1
        }
        assertTrue("scan: correct is the unique longest in ${longest * 100 / all.size}%", longest.toDouble() / all.size <= 0.40)
    }

    @Test fun textIsCleanOfSourceArtifacts() {
        excerpts.forEach { p ->
            assertFalse("${p.id} has footnote marker", Regex("\\[\\d+\\]").containsMatchIn(p.text))
            assertFalse("${p.id} has underscore emphasis", p.text.contains('_'))
            assertFalse("${p.id} has Gutenberg text", p.text.contains("Project Gutenberg", ignoreCase = true))
            assertFalse("${p.id} runs into a chapter heading", Regex("(?m)^(CHAPTER|STAVE|PART) [IVXLC0-9]+").containsMatchIn(p.text))
        }
    }

    @Test fun metadataIsPresent() {
        excerpts.forEach {
            assertTrue(it.title.isNotBlank()); assertTrue(!it.bookTitle.isNullOrBlank()); assertTrue(!it.author.isNullOrBlank())
        }
    }
}
