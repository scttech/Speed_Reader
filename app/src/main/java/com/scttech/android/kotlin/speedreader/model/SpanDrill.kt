package com.scttech.android.kotlin.speedreader.model

import kotlin.math.abs
import kotlin.random.Random

const val SPAN_MIN_WORDS = 2
const val SPAN_MAX_WORDS = 8
const val SPAN_START_WORDS = 3
const val SPAN_ROUNDS = 12
const val SPAN_FLASH_MILLIS = 300L

/** Current phrase width (in words) and consecutive-correct streak. */
data class SpanLevel(val width: Int = SPAN_START_WORDS, val streak: Int = 0)

/** Two correct in a row widens the phrase by a word; any miss narrows it. */
fun nextSpanLevel(level: SpanLevel, correct: Boolean): SpanLevel =
    if (correct) {
        val streak = level.streak + 1
        if (streak >= 2) SpanLevel((level.width + 1).coerceAtMost(SPAN_MAX_WORDS), 0)
        else level.copy(streak = streak)
    } else {
        SpanLevel((level.width - 1).coerceAtLeast(SPAN_MIN_WORDS), 0)
    }

data class SpanQuestion(val phrase: String, val options: List<String>, val correctIndex: Int)

/** Words that can stand in for [original]: same class and capitalisation, similar length (relaxed if none). */
internal fun replacementCandidates(pool: List<String>, original: String, phrase: List<String>, previous: String? = null): List<String> {
    fun startsWithVowel(w: String) = w.first().lowercaseChar() in "aeiou"
    fun agrees(w: String) = when (previous?.lowercase()) {          // "a old" / "an cat" would give the answer away
        "a" -> !startsWithVowel(w)
        "an" -> startsWithVowel(w)
        else -> true
    }
    fun base(w: String) = !w.equals(original, true) && w !in phrase && agrees(w) &&
        w.first().isUpperCase() == original.first().isUpperCase()
    val cls = wordClass(original)
    val strict = pool.filter { base(it) && wordClass(it) == cls && abs(it.length - original.length) <= 1 }
    if (strict.isNotEmpty()) return strict
    val looser = pool.filter { base(it) && wordClass(it) == cls && abs(it.length - original.length) <= 3 }
    if (looser.isNotEmpty()) return looser
    return pool.filter { base(it) && abs(it.length - original.length) <= 1 }
}

private fun stripPunctuation(word: String): String = word.trim { !it.isLetterOrDigit() }

/** Sentences as lists of bare words (punctuation trimmed from word edges). */
fun splitSentences(texts: List<String>): List<List<String>> =
    texts.flatMap { it.split(Regex("(?<=[.!?])\\s+")) }
        .map { s -> s.split(Regex("\\s+")).map(::stripPunctuation).filter { it.isNotEmpty() } }
        .filter { it.size >= SPAN_MIN_WORDS }

private val DETERMINERS = setOf("the", "a", "an", "this", "that", "these", "those", "his", "her", "its", "their", "our", "my", "your", "each", "every", "some", "any", "no")
private val PREPOSITIONS = setOf("of", "in", "on", "at", "to", "for", "with", "by", "from", "into", "over", "under", "through", "about", "after", "before", "between", "against", "during", "without", "upon", "near", "across", "toward", "towards")
private val CONJUNCTIONS_PRONOUNS = setOf("and", "but", "or", "so", "if", "when", "while", "as", "than", "because", "he", "she", "it", "they", "we", "you", "i", "him", "them", "who", "which", "what", "there", "then", "not")
private val MODALS = setOf("will", "would", "can", "could", "may", "might", "must", "shall", "should")
private val BE_FORMS = setOf("is", "are", "was", "were")
private val HAVE_FORMS = setOf("has", "have", "had")
private val DO_FORMS = setOf("do", "did", "does")

/**
 * A rough part-of-speech class, good enough that swapping a word for another of the same class
 * usually keeps a phrase grammatical (so the right answer cannot be spotted by grammar alone).
 */
fun wordClass(word: String): String {
    val w = word.lowercase()
    return when {
        w in DETERMINERS -> "det"
        w in PREPOSITIONS -> "prep"
        w in CONJUNCTIONS_PRONOUNS -> "conj"
        w in MODALS -> "modal"
        w in BE_FORMS -> "be"
        w in HAVE_FORMS -> "have"
        w in DO_FORMS -> "do"
        w.length > 4 && w.endsWith("ly") -> "adverb"
        w.length > 5 && w.endsWith("ing") -> "ing"
        w.length > 4 && w.endsWith("ed") -> "ed"
        w.length > 3 && w.endsWith("s") && !w.endsWith("ss") -> "plural"
        else -> "other"
    }
}

/**
 * A phrase of [width] consecutive words, plus three look-alike distractors that each differ by one
 * word of similar length, shuffled together with the real phrase.
 */
fun makeSpanQuestion(sentences: List<List<String>>, width: Int, random: Random): SpanQuestion {
    val w = width.coerceIn(SPAN_MIN_WORDS, SPAN_MAX_WORDS)
    var eligible = sentences.filter { it.size >= w }
    val useWidth = if (eligible.isEmpty()) sentences.minOf { it.size }.also { eligible = sentences.filter { s -> s.size >= it } } else w
    val sentence = eligible.random(random)
    val start = random.nextInt(sentence.size - useWidth + 1)
    val phrase = sentence.subList(start, start + useWidth)
    val pool = sentences.flatten().filter { it.length >= 3 && it.none(Char::isDigit) }.distinct()

    val options = linkedSetOf(phrase.joinToString(" "))
    var attempts = 0
    while (options.size < 4 && attempts < 200) {
        attempts++
        // Prefer words whose swap stays grammatical (function words, -ing/-ed/-ly/plural forms);
        // only swap a plain content word when the phrase has nothing else.
        val swappable = phrase.indices.filter { wordClass(phrase[it]) != "other" }
        val idx = if (swappable.isNotEmpty() && random.nextInt(10) < 9) swappable.random(random) else random.nextInt(useWidth)
        val original = phrase[idx]
        val candidates = replacementCandidates(pool, original, phrase, previous = phrase.getOrNull(idx - 1))
        if (candidates.isEmpty()) continue
        val changed = phrase.toMutableList().also { it[idx] = candidates.random(random) }
        options += changed.joinToString(" ")
    }
    while (options.size < 4) {                       // extremely unlikely fallback: unrelated phrases of the same width
        val s = eligible.random(random); val st = random.nextInt(s.size - useWidth + 1)
        options += s.subList(st, st + useWidth).joinToString(" ")
    }
    val shuffled = options.toList().shuffled(random)
    return SpanQuestion(phrase.joinToString(" "), shuffled, shuffled.indexOf(phrase.joinToString(" ")))
}
