package com.scttech.android.kotlin.speedreader.model

import kotlin.math.roundToInt

data class Question(
    val text: String,
    val options: List<String>,
    /** Index into [options]. */
    val correctIndex: Int,
)

/** A fact-hunt question for Skim & scan, with the sentence of the excerpt that holds its answer. */
data class ScanQuestion(val question: Question, val evidence: String)

enum class PassageKind { FICTION, NONFICTION }

data class Passage(
    val id: String,
    val title: String,
    val text: String,
    val questions: List<Question>,
    val bookTitle: String? = null,
    val author: String? = null,
    val kind: PassageKind? = null,
    val scanQuestions: List<ScanQuestion> = emptyList(),
) {
    val wordCount: Int = countWords(text)
}

/** One skim-and-scan round: find the answer to [question] somewhere in [passage]. */
data class ScanTask(val passage: Passage, val question: Question, val evidence: String)

/** Words scanned per minute, counted only when the answer was found. */
fun scanWpm(wordCount: Int, seconds: Int, correct: Boolean): Int =
    if (!correct) 0 else (wordCount * 60.0 / seconds.coerceAtLeast(1)).roundToInt()

fun countWords(text: String): Int = text.split(Regex("\\s+")).count { it.isNotBlank() }

/** Raw reading speed in words per minute; elapsed time is floored at 1s to avoid divide-by-zero. */
fun rawWpm(wordCount: Int, elapsedMillis: Long): Int {
    val minutes = elapsedMillis.coerceAtLeast(1_000L) / 60_000.0
    return (wordCount / minutes).roundToInt()
}

fun comprehension(correct: Int, total: Int): Float =
    if (total <= 0) 0f else correct.toFloat() / total
