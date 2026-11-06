package com.scttech.android.kotlin.speedreader.ui.drills

import android.os.SystemClock
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scttech.android.kotlin.speedreader.model.GuidedLine
import com.scttech.android.kotlin.speedreader.model.beatsOf
import com.scttech.android.kotlin.speedreader.model.dwellMillis
import com.scttech.android.kotlin.speedreader.ui.theme.RaceOrange
import kotlinx.coroutines.delay

/** Title bar + content, shared by every drill screen. */
@Composable
internal fun DrillScaffold(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
        content()
    }
}

/**
 * Lines of text with a pacer that visits one chunk (stop) at a time. The dwell on each chunk is
 * proportional to its word count at the speed [wpmAt] reports for the elapsed time, on a fixed
 * schedule so timing never drifts. Calls [onBeat] at each stop and [onFinished] after the last one.
 */
@Composable
internal fun GuidedLines(
    lines: List<GuidedLine>,
    wpmAt: (elapsedMillis: Long) -> Int,
    fontSize: Int,
    modifier: Modifier = Modifier,
    onBeat: (wpm: Int) -> Unit = {},
    onFinished: () -> Unit = {},
) {
    val beats = remember(lines) { beatsOf(lines) }
    var beatIndex by remember(lines) { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val currentWpmAt by rememberUpdatedState(wpmAt)
    val currentOnBeat by rememberUpdatedState(onBeat)
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(lines) {
        val startedAt = SystemClock.elapsedRealtime()
        var due = startedAt
        for ((i, beat) in beats.withIndex()) {
            beatIndex = i
            val wpm = currentWpmAt(due - startedAt)
            currentOnBeat(wpm)
            due += dwellMillis(beat.words, wpm)
            delay((due - SystemClock.elapsedRealtime()).coerceAtLeast(0L))
        }
        currentOnFinished()
    }

    val active = beats.getOrNull(beatIndex)
    LaunchedEffect(active?.line) {
        active?.let { listState.animateScrollToItem((it.line - 3).coerceAtLeast(0)) }
    }

    LazyColumn(state = listState, modifier = modifier.fillMaxSize(), userScrollEnabled = false) {
        itemsIndexed(lines) { index, line ->
            val text = buildAnnotatedString {
                if (active != null && active.line == index) {
                    val r = line.chunkRange(active.chunk)
                    append(line.text.substring(0, r.first))
                    withStyle(SpanStyle(background = RaceOrange.copy(alpha = 0.45f), color = Color.White, fontWeight = FontWeight.Bold)) {
                        append(line.text.substring(r.first, r.last + 1))
                    }
                    append(line.text.substring(r.last + 1))
                } else append(line.text)
            }
            Text(
                text = text,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.7f).sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            )
        }
    }
}
