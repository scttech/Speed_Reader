package com.scttech.android.kotlin.speedreader.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.scttech.android.kotlin.speedreader.progress.ProgressSummary
import com.scttech.android.kotlin.speedreader.progress.RecentItem
import com.scttech.android.kotlin.speedreader.ui.components.ChartReference
import com.scttech.android.kotlin.speedreader.ui.components.LineChart
import com.scttech.android.kotlin.speedreader.ui.drills.DrillScaffold
import java.text.DateFormat
import java.util.Date

@Composable
internal fun ProgressRoute(onBack: () -> Unit, viewModel: ProgressViewModel = hiltViewModel()) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    DrillScaffold(title = "Progress", onBack = onBack) {
        val s = summary
        if (s == null) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
            }
        } else {
            ProgressContent(s)
        }
    }
}

@Composable
internal fun ProgressContent(s: ProgressSummary) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        LevelCard(s)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Day streak", "${s.streak}", "best ${s.bestStreak}", Modifier.weight(1f))
            StatTile("Best", if (s.hasSessions) "${s.bestEffectiveWpm}" else "–", "effective WPM", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Last 5 avg", if (s.hasSessions) "${s.recentAverageEffectiveWpm}" else "–", "effective WPM", Modifier.weight(1f))
            StatTile("Sessions", "${s.sessionCount}", "tests + practice", Modifier.weight(1f))
        }

        if (!s.hasSessions) {
            EmptyHint()
        } else {
            ChartCard("Effective WPM", "Reading speed × comprehension, latest ${s.effectiveSeries.size} ${if (s.effectiveSeries.size == 1) "session" else "sessions"}") {
                LineChart(s.effectiveSeries, references = listOf(ChartReference(s.goalWpm, "goal ${s.goalWpm}")))
            }
            ChartCard("Comprehension", "Percent correct — 80% raises your target, below 60% lowers it") {
                LineChart(s.comprehensionSeries, references = listOf(ChartReference(80, "80%")), yMax = 100)
            }
        }

        if (s.drillBests.isNotEmpty()) {
            SectionCard("Drill bests") {
                s.drillBests.forEach { d ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(d.label, style = MaterialTheme.typography.titleSmall)
                            Text("${d.count} ${if (d.count == 1) "session" else "sessions"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(d.best, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        if (s.recent.isNotEmpty()) {
            SectionCard("Recent activity") {
                s.recent.forEach { RecentRow(it) }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LevelCard(s: ProgressSummary) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(s.title.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${s.level}", fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text(" / 100", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 10.dp))
            }
            LinearProgressIndicator(progress = { s.level / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp))
            Text(
                if (s.hasSessions) "${s.latestEffectiveWpm} effective WPM now, toward your goal of ${s.goalWpm}"
                else "Goal: ${s.goalWpm} effective WPM",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, caption: String, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ChartCard(title: String, caption: String, chart: @Composable () -> Unit) {
    SectionCard(title) {
        Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        chart()
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun EmptyHint() {
    SectionCard("Nothing to chart yet") {
        Text(
            "Take the baseline test or a practice reading and your speed and comprehension will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RecentRow(item: RecentItem) {
    val whenText = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(item.timestampMillis))
    Column(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(item.title, style = MaterialTheme.typography.titleSmall)
            Text(whenText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(item.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
