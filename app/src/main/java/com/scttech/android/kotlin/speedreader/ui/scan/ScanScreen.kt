package com.scttech.android.kotlin.speedreader.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.speedreader.ui.drills.DrillScaffold
import com.scttech.android.kotlin.speedreader.ui.reading.StatRow
import com.scttech.android.kotlin.speedreader.ui.theme.RaceOrange

@Composable
internal fun ScanRoute(onBack: () -> Unit, viewModel: ScanViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DrillScaffold(title = "Skim & scan", onBack = onBack) {
        when (val s = state) {
            ScanUiState.Intro -> Intro(viewModel::start)
            is ScanUiState.Scanning -> Scanning(s, viewModel::found)
            is ScanUiState.Answering -> Answering(s, viewModel::answer)
            is ScanUiState.Result -> Result(s, viewModel::again, viewModel::review, onBack)
            is ScanUiState.Review -> Review(s.result, viewModel::closeReview)
        }
    }
}

@Composable
private fun Intro(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Hunt for one fact", style = MaterialTheme.typography.headlineSmall)
        Text(
            "You'll see a question first, then a passage. You don't need to read every word: sweep your eyes " +
                "over the text, look for the answer, and tap “Found it” as soon as you spot it.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            "Then you'll pick the answer from four choices. Your scan speed counts only when you are right, " +
                "so rushing without finding the fact scores nothing.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Start", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Scanning(s: ScanUiState.Scanning, onFound: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("FIND THE ANSWER TO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(s.task.question.text, style = MaterialTheme.typography.titleMedium)
            }
        }
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text(s.task.passage.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            s.task.passage.text.split("\n\n").forEach { paragraph ->
                Text(paragraph, fontSize = 17.sp, lineHeight = 26.sp)
                Spacer(Modifier.height(12.dp))
            }
        }
        Button(onClick = onFound, modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp)) {
            Text("Found it", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Answering(s: ScanUiState.Answering, onAnswer: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Which is it?", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(s.task.question.text, style = MaterialTheme.typography.titleLarge)
        s.task.question.options.forEachIndexed { i, option ->
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
private fun Result(s: ScanUiState.Result, onAgain: () -> Unit, onReview: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (s.correct) {
            Text("Found it!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.tertiary)
            Text("${s.scanWpm}", fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text("scan WPM", style = MaterialTheme.typography.titleSmall)
        } else {
            Text("Not quite", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
            Text(
                "The answer was: ${s.task.question.options[s.task.question.correctIndex]}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(8.dp))
        StatRow("Time to find it", "${s.seconds}s")
        StatRow("Passage length", "${s.task.passage.wordCount} words")
        StatRow("From", s.task.passage.bookTitle ?: s.task.passage.title)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onAgain, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Next passage") }
        OutlinedButton(onClick = onReview, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Show where the answer was", color = MaterialTheme.colorScheme.onBackground)
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Back to home", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

/** The passage again, scrolled to the sentence that holds the answer, which is highlighted. */
@Composable
private fun Review(result: ScanUiState.Result, onClose: () -> Unit) {
    val task = result.task
    val text = task.passage.text
    val start = text.indexOf(task.evidence).coerceAtLeast(0)
    val end = (start + task.evidence.length).coerceAtMost(text.length)
    val highlighted = remember(text, task.evidence) {
        buildAnnotatedString {
            append(text.substring(0, start))
            withStyle(SpanStyle(background = RaceOrange.copy(alpha = 0.45f), color = Color.White, fontWeight = FontWeight.SemiBold)) {
                append(text.substring(start, end))
            }
            append(text.substring(end))
        }
    }
    val scroll = rememberScrollState()
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    var viewport by remember { mutableIntStateOf(0) }
    LaunchedEffect(layout, viewport) {
        val l = layout ?: return@LaunchedEffect
        val top = l.getLineTop(l.getLineForOffset(start))
        scroll.animateScrollTo((top - viewport * 0.25f).toInt().coerceAtLeast(0))
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(task.question.text, style = MaterialTheme.typography.titleSmall)
                Text(
                    "Answer: " + task.question.options[task.question.correctIndex],
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f).onSizeChanged { viewport = it.height }.verticalScroll(scroll).padding(horizontal = 20.dp),
        ) {
            Text(highlighted, fontSize = 17.sp, lineHeight = 26.sp, onTextLayout = { layout = it })
            Spacer(Modifier.height(12.dp))
        }
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp)) { Text("Back") }
    }
}
