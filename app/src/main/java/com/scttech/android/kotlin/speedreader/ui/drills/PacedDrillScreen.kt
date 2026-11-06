package com.scttech.android.kotlin.speedreader.ui.drills

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.speedreader.model.rampWpm
import com.scttech.android.kotlin.speedreader.ui.reading.StatRow
import kotlinx.coroutines.delay

@Composable
internal fun PacedDrillRoute(onBack: () -> Unit, viewModel: PacedDrillViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val kind = viewModel.kind
    var sound by rememberSaveable { mutableStateOf(true) }

    DrillScaffold(title = kind.title, onBack = onBack) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val s = state) {
                PacedDrillUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is PacedDrillUiState.Intro -> Intro(s, sound, { sound = it }, viewModel::adjustSpeed, viewModel::start)
                is PacedDrillUiState.Running -> Running(s, sound, viewModel::finish)
                is PacedDrillUiState.Finished -> Finished(s, viewModel::again, onBack)
            }
        }
    }
}

@Composable
private fun Intro(
    s: PacedDrillUiState.Intro,
    sound: Boolean,
    onSound: (Boolean) -> Unit,
    onAdjust: (Int) -> Unit,
    onStart: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(s.kind.instructions, style = MaterialTheme.typography.bodyLarge)
        StatRow("Duration", "${s.kind.durationSeconds} seconds")
        Text(
            if (s.kind.ramps) "Starting speed" else "Speed",
            style = MaterialTheme.typography.titleMedium,
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { onAdjust(-25) }) { Text("-25", color = MaterialTheme.colorScheme.onBackground) }
            Text("${s.startWpm} WPM", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = { onAdjust(25) }) { Text("+25", color = MaterialTheme.colorScheme.onBackground) }
        }
        if (s.kind.ramps) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Beat sound", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Switch(checked = sound, onCheckedChange = onSound)
            }
        }
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Start", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Running(
    s: PacedDrillUiState.Running,
    sound: Boolean,
    onFinish: (endWpm: Int, seconds: Int) -> Unit,
) {
    val kind = s.kind
    var currentWpm by remember { mutableIntStateOf(s.startWpm) }
    val startedAt = remember { SystemClock.elapsedRealtime() }
    fun elapsedSeconds() = ((SystemClock.elapsedRealtime() - startedAt) / 1000).toInt()

    val tone = remember { if (kind.ramps && sound) ToneGenerator(AudioManager.STREAM_MUSIC, 35) else null }
    DisposableEffect(Unit) { onDispose { tone?.release() } }

    LaunchedEffect(Unit) {
        delay(kind.durationSeconds * 1000L)
        onFinish(currentWpm, kind.durationSeconds)
    }
    val remaining by produceState(kind.durationSeconds) {
        while (true) {
            value = (kind.durationSeconds - elapsedSeconds()).coerceAtLeast(0)
            delay(250)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("$currentWpm WPM", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text("${remaining}s", style = MaterialTheme.typography.titleMedium)
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            GuidedLines(
                lines = s.lines,
                wpmAt = { t -> if (kind.ramps) rampWpm(s.startWpm, t) else s.startWpm },
                fontSize = if (kind == PacedDrillKind.COLUMN) 26 else 22,
                onBeat = { wpm ->
                    currentWpm = wpm
                    tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
                },
                onFinished = { onFinish(currentWpm, elapsedSeconds()) },
            )
            if (kind == PacedDrillKind.COLUMN) {
                val guide = MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
                Canvas(Modifier.matchParentSize()) {
                    drawLine(guide, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = 3f)
                }
            }
        }
        OutlinedButton(onClick = { onFinish(currentWpm, elapsedSeconds()) }, modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp)) {
            Text("Stop", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun Finished(s: PacedDrillUiState.Finished, onAgain: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (s.kind.ramps) {
            Text("You kept pace up to", style = MaterialTheme.typography.titleMedium)
            Text("${s.endWpm}", fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text("WPM", style = MaterialTheme.typography.titleSmall)
            StatRow("Started at", "${s.startWpm} WPM")
            StatRow("Gain", "+${s.endWpm - s.startWpm} WPM")
        } else {
            Text("Drill complete", style = MaterialTheme.typography.headlineSmall)
            Text("${s.startWpm}", fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text("WPM", style = MaterialTheme.typography.titleSmall)
        }
        StatRow("Time", "${s.seconds}s")
        Button(onClick = onAgain, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Again") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Back to drills", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}
