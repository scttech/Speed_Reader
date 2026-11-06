package com.scttech.android.kotlin.speedreader.ui.reading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scttech.android.kotlin.speedreader.model.Question

/** One multiple-choice question at a time; used by both the baseline test and practice reading. */
@Composable
internal fun QuizContent(questions: List<Question>, index: Int, onAnswer: (Int) -> Unit) {
    val total = questions.size
    val question = questions[index]
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Question ${index + 1} of $total",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinearProgressIndicator(progress = { index / total.toFloat() }, modifier = Modifier.fillMaxWidth())
        Text(question.text, style = MaterialTheme.typography.titleLarge)
        question.options.forEachIndexed { i, option ->
            OutlinedButton(onClick = { onAnswer(i) }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    option,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

/** Shown instead of the quiz when the measured speed is too high to be a real read. */
@Composable
internal fun TooFastContent(rawWpm: Int, onRetry: () -> Unit, onExit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Too fast to count", style = MaterialTheme.typography.headlineSmall)
        Text("$rawWpm WPM", fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
        Text(
            "That is faster than anyone can truly read, so this attempt wasn't counted and your gauge and " +
                "target are unchanged. Read the whole passage at a natural pace, then tap Done.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Try again") }
        OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Back to gauge", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
internal fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
