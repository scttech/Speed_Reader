package com.scttech.android.kotlin.speedreader.ui.drills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private data class DrillEntry(val route: String, val title: String, val blurb: String, val icon: ImageVector)

private val entries = listOf(
    DrillEntry(
        "drill_span", "Span stretch",
        "Flash a phrase for a split second, then pick what you saw. Widens how many words you take in at once.",
        Icons.Filled.Visibility,
    ),
    DrillEntry(
        "drill_paced/column", "Column drill",
        "Narrow lines with a centre guide. Your eyes move down only — no left-right wandering.",
        Icons.Filled.ViewColumn,
    ),
    DrillEntry(
        "drill_paced/twostop", "Two-stop lines",
        "Each line gets just two fixations: one left of centre, one right. No word-hopping.",
        Icons.Filled.Equalizer,
    ),
    DrillEntry(
        "drill_paced/metronome", "Metronome ramp",
        "A beat that creeps faster every 15 seconds, pulling your brain past its comfortable pace.",
        Icons.Filled.Timer,
    ),
)

@Composable
internal fun DrillsRoute(onBack: () -> Unit, onOpen: (String) -> Unit) {
    DrillScaffold(title = "Drills", onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Short exercises that train the eyes. Pacing drills start at your current practice target.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            entries.forEach { e ->
                Card(
                    onClick = { onOpen(e.route) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(e.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(e.title, style = MaterialTheme.typography.titleMedium)
                            Text(e.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
