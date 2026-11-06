package com.scttech.android.kotlin.speedreader.ui.tips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class Tip(val title: String, val body: String)

private val tips = listOf(
    Tip("Comprehension comes first", "Only effective WPM counts (speed x comprehension). If you are guessing the quiz answers, slow down. Speed built on skimming does not last."),
    Tip("Practise a little every day", "Ten to fifteen minutes daily beats one long session a week. Turn on the daily reminder in Settings and protect your streak."),
    Tip("Do a drill before you read", "Warm up with Span stretch or a Column drill for a few minutes, then do a practice reading. The eye habits carry straight over."),
    Tip("Follow the pacer, don't chase it", "Keep your eyes on the highlight and let it pull you along. Resist going back over words. Rereading is the habit that costs the most time."),
    Tip("Stop saying every word in your head", "Hum quietly or tap a finger while you read to occupy your inner voice. Aim to take in whole phrases rather than single words."),
    Tip("Let the target adapt", "Leave the target alone. It rises at 80% comprehension or better and falls below 60%. Sitting in the middle means you are in the right zone."),
    Tip("Use Metronome ramp to stretch", "It speeds up every 15 seconds. Go past the point where it feels comfortable, then drop back to practice reading and it will feel slower."),
    Tip("Skim & scan is a different skill", "Use it when you only need one fact. Sweep the text for keywords instead of reading every line, and treat it as a separate speed from careful reading."),
    Tip("Read in good conditions", "Good light, the phone held steady at a comfortable distance, and no notifications. Test and practise when you are rested for fair, comparable results."),
    Tip("Judge progress over weeks", "A single session is noisy. Watch the last-five average and the charts on the Progress screen, and re-run the baseline test now and then."),
)

@Composable
internal fun TipsScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text("Get the most out of it", style = MaterialTheme.typography.headlineSmall)
        }
        tips.forEachIndexed { index, tip ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("${index + 1}. ${tip.title}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(tip.body, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
