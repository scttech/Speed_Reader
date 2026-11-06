package com.scttech.android.kotlin.speedreader.ui.baseline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.scttech.android.kotlin.speedreader.ui.reading.QuizContent
import com.scttech.android.kotlin.speedreader.ui.reading.TooFastContent
import com.scttech.android.kotlin.speedreader.ui.reading.StatRow

@Composable
internal fun BaselineRoute(
    onExit: () -> Unit,
    viewModel: BaselineViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BaselineScreen(
        state = state,
        onStart = viewModel::start,
        onDoneReading = viewModel::finishReading,
        onAnswer = viewModel::answer,
        onRetry = viewModel::restart,
        onExit = onExit,
    )
}

@Composable
internal fun BaselineScreen(
    state: BaselineUiState,
    onStart: () -> Unit,
    onDoneReading: () -> Unit,
    onAnswer: (Int) -> Unit,
    onRetry: () -> Unit,
    onExit: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            IconButton(onClick = onExit) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text("Baseline test", style = MaterialTheme.typography.titleLarge)
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (state) {
                BaselineUiState.Intro -> IntroContent(onStart)
                is BaselineUiState.Reading -> ReadingContent(state, onDoneReading)
                is BaselineUiState.Quiz -> QuizContent(state.passage.questions, state.index, onAnswer)
                is BaselineUiState.TooFast -> TooFastContent(state.rawWpm, onRetry, onExit)
                BaselineUiState.Saving -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is BaselineUiState.Result -> ResultContent(state, onRetry, onExit)
            }
        }
    }
}

@Composable
private fun IntroContent(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Find your starting line", style = MaterialTheme.typography.headlineMedium)
        Text(
            "You'll read a short passage, then answer five questions about it. " +
                "Your score is your effective WPM: reading speed multiplied by comprehension.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text("For an honest baseline:", style = MaterialTheme.typography.titleMedium)
        Text(
            "•  Read at your normal, comfortable pace.\n" +
                "•  Don't skim, and don't rush to beat the clock.\n" +
                "•  You can't go back to the text once you tap Done.\n" +
                "•  Timing starts the moment you tap Start.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Start", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ReadingContent(state: BaselineUiState.Reading, onDone: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        ) {
            Text(state.passage.title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            state.passage.text.split("\n\n").forEach { paragraph ->
                Text(paragraph, fontSize = 18.sp, lineHeight = 28.sp)
                Spacer(Modifier.height(14.dp))
            }
        }
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
        ) {
            Text("Done reading", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ResultContent(state: BaselineUiState.Result, onRetry: () -> Unit, onExit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Your baseline", style = MaterialTheme.typography.headlineSmall)
        Text(
            "${state.effectiveWpm}",
            fontSize = 72.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text("effective WPM", style = MaterialTheme.typography.titleMedium)
        Text(
            "Level ${state.level} / 100  ·  goal ${state.goalWpm}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.tertiary,
        )
        Spacer(Modifier.height(8.dp))
        StatRow("Reading speed", "${state.rawWpm} WPM")
        StatRow("Comprehension", "${state.correct} of ${state.total} correct")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onExit, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Back to gauge") }
        OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Test again", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}
