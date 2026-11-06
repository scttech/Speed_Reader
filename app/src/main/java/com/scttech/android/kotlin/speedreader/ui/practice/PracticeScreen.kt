package com.scttech.android.kotlin.speedreader.ui.practice

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.speedreader.model.Passage
import com.scttech.android.kotlin.speedreader.model.PassageKind
import com.scttech.android.kotlin.speedreader.model.TargetChange
import com.scttech.android.kotlin.speedreader.ui.reading.PacedReader
import com.scttech.android.kotlin.speedreader.ui.reading.QuizContent
import com.scttech.android.kotlin.speedreader.ui.reading.StatRow
import com.scttech.android.kotlin.speedreader.ui.reading.TooFastContent

@Composable
internal fun PracticeRoute(
    onExit: () -> Unit,
    viewModel: PracticeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PracticeScreen(
        state = state,
        onStart = viewModel::start,
        onDoneReading = viewModel::finishReading,
        onAnswer = viewModel::answer,
        onNext = viewModel::nextExcerpt,
        onExit = onExit,
    )
}

@Composable
internal fun PracticeScreen(
    state: PracticeUiState,
    onStart: () -> Unit,
    onDoneReading: () -> Unit,
    onAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    onExit: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            IconButton(onClick = onExit) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text("Practice reading", style = MaterialTheme.typography.titleLarge)
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (state) {
                PracticeUiState.Loading, PracticeUiState.Saving ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                is PracticeUiState.TooFast -> TooFastContent(state.rawWpm, onNext, onExit)
                is PracticeUiState.Intro -> IntroContent(state, onStart)
                is PracticeUiState.Reading -> ReadingContent(state, onDoneReading)
                is PracticeUiState.Quiz -> QuizContent(state.passage.questions, state.index, onAnswer)
                is PracticeUiState.Result -> ResultContent(state, onNext, onExit)
            }
        }
    }
}

private fun Passage.sourceLine(): String =
    listOfNotNull(bookTitle, author).joinToString(" · ")

private fun Passage.kindLabel(): String = when (kind) {
    PassageKind.FICTION -> "Fiction"
    PassageKind.NONFICTION -> "Non-fiction"
    null -> ""
}

@Composable
private fun IntroContent(state: PracticeUiState.Intro, onStart: () -> Unit) {
    val p = state.passage
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(p.kindLabel().uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
        Text(p.title, style = MaterialTheme.typography.headlineMedium)
        Text(p.sourceLine(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        StatRow("Length", "${p.wordCount} words")
        StatRow("Target speed", "${state.targetWpm} WPM")
        StatRow("Estimated time", formatDuration(p.wordCount * 60 / state.targetWpm))
        Text(
            "An orange pacer will move through the text at your target speed. Try to keep your eyes on it " +
                "without going back. Tap “Done reading” when you finish, then answer five questions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Start", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

internal fun formatDuration(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return if (m == 0) "${s}s" else "${m}m ${s.toString().padStart(2, '0')}s"
}

@Composable
private fun ReadingContent(state: PracticeUiState.Reading, onDone: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "${state.passage.title}  ·  ${state.targetWpm} WPM",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        Box(modifier = Modifier.weight(1f)) {
            PacedReader(text = state.passage.text.replace("\n\n", "\n\n"), targetWpm = state.targetWpm)
        }
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp)) {
            Text("Done reading", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ResultContent(state: PracticeUiState.Result, onNext: () -> Unit, onExit: () -> Unit) {
    val (headline, color) = when (state.change) {
        TargetChange.RAISED -> "Target raised" to MaterialTheme.colorScheme.tertiary
        TargetChange.LOWERED -> "Target lowered" to MaterialTheme.colorScheme.error
        TargetChange.HELD -> "Target held" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Effective speed", style = MaterialTheme.typography.titleMedium)
        Text("${state.effectiveWpm}", fontSize = 72.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text("effective WPM", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        StatRow("Reading speed", "${state.rawWpm} WPM")
        StatRow("Comprehension", "${state.correct} of ${state.total} correct")
        StatRow("Target this time", "${state.targetWpm} WPM")
        Spacer(Modifier.height(8.dp))
        Text(headline, style = MaterialTheme.typography.titleMedium, color = color)
        Text("Next target: ${state.nextTargetWpm} WPM", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Next excerpt") }
        OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Back to gauge", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}
