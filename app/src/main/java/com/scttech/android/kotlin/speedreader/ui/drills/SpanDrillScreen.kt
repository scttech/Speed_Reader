package com.scttech.android.kotlin.speedreader.ui.drills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.speedreader.model.SPAN_FLASH_MILLIS
import com.scttech.android.kotlin.speedreader.model.SPAN_ROUNDS
import com.scttech.android.kotlin.speedreader.ui.reading.StatRow

@Composable
internal fun SpanDrillRoute(onBack: () -> Unit, viewModel: SpanDrillViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DrillScaffold(title = "Span stretch", onBack = onBack) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val s = state) {
                SpanDrillUiState.Intro -> Intro(viewModel::start)
                is SpanDrillUiState.Fixation -> Centered { Text("+", fontSize = 48.sp, color = MaterialTheme.colorScheme.primary) }
                is SpanDrillUiState.Flash -> Centered {
                    Text(
                        s.phrase,
                        fontSize = (700f / s.phrase.length).coerceIn(13f, 28f).sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center,
                    )
                }
                is SpanDrillUiState.Choose -> Choose(s, viewModel::answer)
                is SpanDrillUiState.Feedback -> Centered {
                    Text(
                        if (s.correct) "Correct" else "Not quite",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (s.correct) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                    )
                    Text(s.phrase, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
                is SpanDrillUiState.Summary -> Summary(s, viewModel::start, onBack)
            }
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { content() }
}

@Composable
private fun Intro(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Take in more words per glance", style = MaterialTheme.typography.headlineSmall)
        Text(
            "A phrase flashes for ${SPAN_FLASH_MILLIS} ms. Keep your eyes on the centre and read it in one look, " +
                "then choose the phrase you saw. Options differ by a single word.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            "Two right answers in a row make the phrase one word wider; a miss makes it narrower. $SPAN_ROUNDS rounds.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Start", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Choose(state: SpanDrillUiState.Choose, onAnswer: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Round ${state.round} of $SPAN_ROUNDS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LinearProgressIndicator(progress = { (state.round - 1) / SPAN_ROUNDS.toFloat() }, modifier = Modifier.fillMaxWidth())
        Text("Which phrase did you see?", style = MaterialTheme.typography.titleLarge)
        state.question.options.forEachIndexed { i, option ->
            OutlinedButton(onClick = { onAnswer(i) }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    option,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun Summary(s: SpanDrillUiState.Summary, onAgain: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Widest phrase read", style = MaterialTheme.typography.titleMedium)
        Text(
            if (s.widestRead > 0) "${s.widestRead} words" else "—",
            fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary,
        )
        StatRow("Correct", "${s.correct} of ${s.total}")
        Button(onClick = onAgain, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Again") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Back to drills", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}
