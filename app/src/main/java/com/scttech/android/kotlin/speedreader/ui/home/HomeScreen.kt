package com.scttech.android.kotlin.speedreader.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.speedreader.R
import com.scttech.android.kotlin.speedreader.ui.components.SpeedGauge

@Composable
internal fun HomeRoute(
    onOpenSettings: () -> Unit,
    onStartBaseline: () -> Unit,
    onStartPractice: () -> Unit,
    onStartDrills: () -> Unit,
    onStartScan: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenTips: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onOpenSettings = onOpenSettings,
        onStartBaseline = onStartBaseline,
        onStartPractice = onStartPractice,
        onStartDrills = onStartDrills,
        onStartScan = onStartScan,
        onOpenProgress = onOpenProgress,
        onOpenTips = onOpenTips,
    )
}

private data class Module(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val action: ModuleAction? = null,
)

private enum class ModuleAction { BASELINE, PRACTICE, DRILLS, SCAN, PROGRESS, TIPS }

private val modules = listOf(
    Module("Baseline test", "Measure your effective WPM", Icons.Filled.Speed, ModuleAction.BASELINE),
    Module("Drills", "Vision span, pacer, two-stop, metronome", Icons.Filled.Visibility, ModuleAction.DRILLS),
    Module("Practice reading", "Adaptive target speed with quizzes", Icons.AutoMirrored.Filled.MenuBook, ModuleAction.PRACTICE),
    Module("Skim & scan", "Hunt for one fact in a passage", Icons.Filled.FitnessCenter, ModuleAction.SCAN),
    Module("Progress", "Charts, streaks and levels", Icons.Filled.BarChart, ModuleAction.PROGRESS),
    Module("Get the most out of it", "Tips to improve your reading speed", Icons.Filled.Lightbulb, ModuleAction.TIPS),
)

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onOpenSettings: () -> Unit,
    onStartBaseline: () -> Unit,
    onStartPractice: () -> Unit,
    onStartDrills: () -> Unit,
    onStartScan: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenTips: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.logo_speed_reader),
                contentDescription = "Speed Reader: 0 to 100",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().height(150.dp),
            )
            SpeedGauge(
                level = state.level,
                effectiveWpm = state.effectiveWpm,
                goalWpm = state.goalWpm,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Spacer(Modifier.height(8.dp))
            modules.forEach { module ->
                ModuleCard(
                    module,
                    onClick = when (module.action) {
                        ModuleAction.BASELINE -> onStartBaseline
                        ModuleAction.PRACTICE -> onStartPractice
                        ModuleAction.DRILLS -> onStartDrills
                        ModuleAction.SCAN -> onStartScan
                        ModuleAction.PROGRESS -> onOpenProgress
                        ModuleAction.TIPS -> onOpenTips
                        null -> null
                    },
                )
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(16.dp))
        }
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.align(Alignment.TopEnd).safeDrawingPadding(),
        ) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun ModuleCard(module: Module, onClick: (() -> Unit)?) {
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val body: @Composable ColumnScope.() -> Unit = { ModuleCardBody(module, showSoon = onClick == null) }
    if (onClick != null) {
        Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = colors, content = body)
    } else {
        Card(modifier = Modifier.fillMaxWidth(), colors = colors, content = body)
    }
}

@Composable
private fun ModuleCardBody(module: Module, showSoon: Boolean) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(module.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f)) {
            Text(module.title, style = MaterialTheme.typography.titleMedium)
            Text(
                module.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (showSoon) {
            Text(
                "Soon",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
