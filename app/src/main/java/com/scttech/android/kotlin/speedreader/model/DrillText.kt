package com.scttech.android.kotlin.speedreader.model

/** One displayed line, split into the "stops" the pacer visits (one chunk = one eye fixation). */
data class GuidedLine(val chunks: List<String>) {
    val text: String = chunks.joinToString(" ")

    /** Character range of chunk [index] within [text]. */
    fun chunkRange(index: Int): IntRange {
        var start = 0
        for (i in 0 until index) start += chunks[i].length + 1
        return start until start + chunks[index].length
    }
}

/** A single pacer position: which chunk of which line, and how many words it holds. */
data class Beat(val line: Int, val chunk: Int, val words: Int)

fun beatsOf(lines: List<GuidedLine>): List<Beat> =
    lines.flatMapIndexed { li, line ->
        line.chunks.mapIndexed { ci, chunk -> Beat(li, ci, chunk.split(' ').count { it.isNotBlank() }) }
    }

/** Milliseconds to dwell on [words] words so that overall speed equals [wpm]. */
fun dwellMillis(words: Int, wpm: Int): Long = 60_000L * words / wpm.coerceAtLeast(1)

fun corpusWords(texts: List<String>): List<String> =
    texts.flatMap { t -> t.split(Regex("\\s+")).filter { it.isNotBlank() } }

/** Narrow column, one stop per line (eye-control drill: eyes move down only). */
fun columnLines(words: List<String>, wordsPerLine: Int = 3): List<GuidedLine> =
    words.chunked(wordsPerLine).map { GuidedLine(listOf(it.joinToString(" "))) }

private fun splitInTwo(words: List<String>): GuidedLine {
    if (words.size < 2) return GuidedLine(listOf(words.joinToString(" ")))
    val half = (words.size + 1) / 2
    return GuidedLine(listOf(words.take(half).joinToString(" "), words.drop(half).joinToString(" ")))
}

/** Lines of up to [maxChars] characters with exactly two stops each (Cutler's "two-stop" method). */
fun twoStopLines(words: List<String>, maxChars: Int = 30): List<GuidedLine> {
    val lines = mutableListOf<GuidedLine>()
    var current = mutableListOf<String>()
    var length = 0
    for (w in words) {
        val needed = if (current.isEmpty()) w.length else length + 1 + w.length
        if (current.isNotEmpty() && needed > maxChars) {
            lines += splitInTwo(current)
            current = mutableListOf()
        }
        length = if (current.isEmpty()) w.length else length + 1 + w.length
        current += w
    }
    if (current.isNotEmpty()) lines += splitInTwo(current)
    return lines
}

const val RAMP_STEP_WPM = 25
const val RAMP_STEP_MILLIS = 15_000L

/** Metronome ramp: speed rises [stepWpm] every [stepMillis], capped at [maxWpm]. */
fun rampWpm(
    startWpm: Int,
    elapsedMillis: Long,
    stepWpm: Int = RAMP_STEP_WPM,
    stepMillis: Long = RAMP_STEP_MILLIS,
    maxWpm: Int = MAX_TARGET_WPM,
): Int = (startWpm + (elapsedMillis / stepMillis).toInt() * stepWpm).coerceAtMost(maxWpm)
