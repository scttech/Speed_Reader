package com.scttech.android.kotlin.speedreader.ui.reading

import android.os.SystemClock
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scttech.android.kotlin.speedreader.ui.theme.RaceOrange
import kotlinx.coroutines.delay

/** Words highlighted at a time by the pacer. */
const val PACER_CHUNK_WORDS = 3

/** Start/end character offsets of every whitespace-separated word. */
internal fun wordRanges(text: String): List<IntRange> =
    Regex("\\S+").findAll(text).map { it.range }.toList()

/** Milliseconds the pacer dwells on one chunk so that the overall speed equals [wpm]. */
internal fun chunkDelayMillis(wpm: Int, chunkWords: Int = PACER_CHUNK_WORDS): Long =
    (60_000L * chunkWords) / wpm.coerceAtLeast(1)

/**
 * Text with a moving highlight (the "pacer") that advances at [targetWpm] and keeps the
 * highlighted line in view. Reading itself is untimed here; the caller measures elapsed time.
 */
@Composable
fun PacedReader(text: String, targetWpm: Int, modifier: Modifier = Modifier) {
    val words = remember(text) { wordRanges(text) }
    var chunk by remember(text) { mutableIntStateOf(0) }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    var viewportHeight by remember { mutableIntStateOf(0) }
    val scroll = rememberScrollState()
    val chunkCount = (words.size + PACER_CHUNK_WORDS - 1) / PACER_CHUNK_WORDS

    LaunchedEffect(text, targetWpm) {
        // Fixed schedule from the start time, so per-step overhead never accumulates into drift.
        val step = chunkDelayMillis(targetWpm)
        val startedAt = SystemClock.elapsedRealtime()
        while (chunk < chunkCount) {
            val due = startedAt + step * (chunk + 1)
            delay((due - SystemClock.elapsedRealtime()).coerceAtLeast(0L))
            chunk = (((SystemClock.elapsedRealtime() - startedAt) / step).toInt()).coerceIn(chunk + 1, chunkCount)
        }
    }

    val highlight = if (chunk < chunkCount) {
        val first = words[chunk * PACER_CHUNK_WORDS].first
        val last = words[minOf(chunk * PACER_CHUNK_WORDS + PACER_CHUNK_WORDS, words.size) - 1].last + 1
        first until last
    } else null

    // Keep the highlighted line about a third of the way down the viewport (scrolls forward only).
    LaunchedEffect(highlight?.first, layout, viewportHeight) {
        val l = layout ?: return@LaunchedEffect
        val start = highlight?.first ?: return@LaunchedEffect
        val top = l.getLineTop(l.getLineForOffset(start))
        val wanted = (top - viewportHeight * 0.3f).toInt()
        if (wanted > scroll.value) scroll.animateScrollTo(wanted)
    }

    val annotated = remember(text, highlight) {
        buildAnnotatedString {
            if (highlight == null) {
                append(text)
            } else {
                append(text.substring(0, highlight.first))
                withStyle(SpanStyle(background = RaceOrange.copy(alpha = 0.40f), color = Color.White, fontWeight = FontWeight.SemiBold)) {
                    append(text.substring(highlight.first, highlight.last + 1))
                }
                append(text.substring(highlight.last + 1))
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { viewportHeight = it.height }
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = annotated,
            fontSize = 18.sp,
            lineHeight = 28.sp,
            onTextLayout = { layout = it },
        )
    }
}
