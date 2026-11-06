package com.scttech.android.kotlin.speedreader.ui.settings

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.speedreader.model.DEFAULT_ADJUST_PERCENT
import com.scttech.android.kotlin.speedreader.model.DEFAULT_GOAL_WPM
import com.scttech.android.kotlin.speedreader.model.LOWER_COMPREHENSION_THRESHOLD
import com.scttech.android.kotlin.speedreader.model.MAX_ADJUST_PERCENT
import com.scttech.android.kotlin.speedreader.model.MAX_GOAL_WPM
import com.scttech.android.kotlin.speedreader.model.MIN_ADJUST_PERCENT
import com.scttech.android.kotlin.speedreader.model.MIN_GOAL_WPM
import com.scttech.android.kotlin.speedreader.model.RAISE_COMPREHENSION_THRESHOLD
import com.scttech.android.kotlin.speedreader.reminder.ReminderNotifier
import com.scttech.android.kotlin.speedreader.reminder.formatMinuteOfDay
import kotlin.math.roundToInt

@Composable
internal fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val goal by viewModel.goalWpm.collectAsStateWithLifecycle()
    val raise by viewModel.raisePercent.collectAsStateWithLifecycle()
    val lower by viewModel.lowerPercent.collectAsStateWithLifecycle()
    val reminderOn by viewModel.reminderEnabled.collectAsStateWithLifecycle()
    val reminderMinute by viewModel.reminderMinuteOfDay.collectAsStateWithLifecycle()
    val resettable by viewModel.resettableCount.collectAsStateWithLifecycle()
    val confirmVisible by viewModel.resetConfirmVisible.collectAsStateWithLifecycle()
    SettingsScreen(
        goalWpm = goal,
        raisePercent = raise,
        lowerPercent = lower,
        reminderEnabled = reminderOn,
        reminderMinuteOfDay = reminderMinute,
        resettableCount = resettable,
        resetConfirmVisible = confirmVisible,
        onReminderEnabledChange = viewModel::setReminderEnabled,
        onReminderTimeChange = viewModel::setReminderTime,
        onRequestReset = viewModel::requestReset,
        onCancelReset = viewModel::cancelReset,
        onConfirmReset = viewModel::confirmReset,
        onGoalChange = viewModel::setGoal,
        onRaiseChange = viewModel::setRaisePercent,
        onLowerChange = viewModel::setLowerPercent,
        onBack = onBack,
    )
}

@Composable
internal fun SettingsScreen(
    goalWpm: Int,
    raisePercent: Int,
    lowerPercent: Int,
    reminderEnabled: Boolean,
    reminderMinuteOfDay: Int,
    resettableCount: Int,
    resetConfirmVisible: Boolean,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderTimeChange: (Int) -> Unit,
    onRequestReset: () -> Unit,
    onCancelReset: () -> Unit,
    onConfirmReset: () -> Unit,
    onGoalChange: (Int) -> Unit,
    onRaiseChange: (Int) -> Unit,
    onLowerChange: (Int) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text("Settings", style = MaterialTheme.typography.headlineSmall)
        }

        SettingSlider(
            title = "Goal (effective WPM)",
            description = "Effective WPM = reading speed x comprehension. Level 100 on the gauge means reaching this goal.",
            value = goalWpm,
            range = MIN_GOAL_WPM..MAX_GOAL_WPM,
            step = 10,
            format = { "$it WPM" },
            onCommit = onGoalChange,
            resetLabel = "Reset to default ($DEFAULT_GOAL_WPM)",
            resetValue = DEFAULT_GOAL_WPM,
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text("Practice target adjustment", style = MaterialTheme.typography.titleLarge)
        Text(
            "After each practice reading your target speed changes based on comprehension: " +
                "${(RAISE_COMPREHENSION_THRESHOLD * 100).roundToInt()}% or better raises it, " +
                "below ${(LOWER_COMPREHENSION_THRESHOLD * 100).roundToInt()}% lowers it, and in between it stays put.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SettingSlider(
            title = "Raise target by",
            description = null,
            value = raisePercent,
            range = MIN_ADJUST_PERCENT..MAX_ADJUST_PERCENT,
            step = 1,
            format = { "+$it%" },
            onCommit = onRaiseChange,
            resetLabel = "Reset to default ($DEFAULT_ADJUST_PERCENT%)",
            resetValue = DEFAULT_ADJUST_PERCENT,
        )

        SettingSlider(
            title = "Lower target by",
            description = null,
            value = lowerPercent,
            range = MIN_ADJUST_PERCENT..MAX_ADJUST_PERCENT,
            step = 1,
            format = { "-$it%" },
            onCommit = onLowerChange,
            resetLabel = "Reset to default ($DEFAULT_ADJUST_PERCENT%)",
            resetValue = DEFAULT_ADJUST_PERCENT,
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        ReminderSection(reminderEnabled, reminderMinuteOfDay, onReminderEnabledChange, onReminderTimeChange)

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        ResetSection(resettableCount, onRequestReset)
    }

    if (resetConfirmVisible) {
        ResetConfirmDialog(resettableCount, onConfirm = onConfirmReset, onDismiss = onCancelReset)
    }
}

@Composable
private fun ReminderSection(
    enabled: Boolean,
    minuteOfDay: Int,
    onEnabledChange: (Boolean) -> Unit,
    onTimeChange: (Int) -> Unit,
) {
    val context = LocalContext.current
    var blocked by remember { mutableStateOf(false) }
    var pickerOpen by remember { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        blocked = !granted
        if (granted) onEnabledChange(true)
    }

    Text("Daily reminder", style = MaterialTheme.typography.titleLarge)
    Text(
        "A notification once a day at the time you choose. It stays quiet on days you have already practised.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Remind me every day", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = enabled,
            onCheckedChange = { wantOn ->
                if (!wantOn) { blocked = false; onEnabledChange(false) }
                else if (ReminderNotifier.hasPermission(context)) { blocked = false; onEnabledChange(true) }
                else permission.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
        )
    }
    if (enabled) {
        OutlinedButton(onClick = { pickerOpen = true }) {
            Text("Reminder time: ${formatMinuteOfDay(minuteOfDay)}", color = MaterialTheme.colorScheme.onBackground)
        }
    }
    if (blocked) {
        Text(
            "Notifications are blocked for Speed Reader. Allow them in your phone's app settings to use the reminder.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
    if (pickerOpen) {
        ReminderTimeDialog(minuteOfDay, onConfirm = { onTimeChange(it); pickerOpen = false }, onDismiss = { pickerOpen = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(initialMinute: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    val state = rememberTimePickerState(initialHour = initialMinute / 60, initialMinute = initialMinute % 60, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reminder time") },
        text = { TimePicker(state = state) },
        confirmButton = { TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) { Text("Set") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ResetSection(resettableCount: Int, onRequestReset: () -> Unit) {
    Text("Reset my speed", style = MaterialTheme.typography.titleLarge)
    Text(
        "If your numbers have gone off the rails, you can wipe your baseline and practice results and start " +
            "again from a fresh baseline test.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    OutlinedButton(
        onClick = onRequestReset,
        enabled = resettableCount > 0,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
    ) { Text(if (resettableCount > 0) "Reset my effective WPM…" else "Nothing to reset yet") }
}

@Composable
private fun ResetConfirmDialog(count: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset your effective WPM?") },
        text = {
            Text(
                "Are you sure? This permanently deletes your $count baseline and practice " +
                    "${if (count == 1) "result" else "results"}. Your gauge goes back to 0 and your practice target " +
                    "starts over. Drill results and your settings are kept.\n\nThis cannot be undone.",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text("Yes, reset")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** A labelled slider that shows a live value and saves it when the user lifts their finger. */
@Composable
private fun SettingSlider(
    title: String,
    description: String?,
    value: Int,
    range: IntRange,
    step: Int,
    format: (Int) -> String,
    onCommit: (Int) -> Unit,
    resetLabel: String,
    resetValue: Int,
) {
    var slider by remember { mutableFloatStateOf(value.toFloat()) }
    LaunchedEffect(value) { slider = value.toFloat() }
    val shown = ((slider / step).roundToInt() * step).coerceIn(range.first, range.last)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (description != null) {
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(format(shown), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Slider(
            value = slider,
            onValueChange = { slider = it },
            onValueChangeFinished = { onCommit(shown) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = { onCommit(resetValue) }) { Text(resetLabel) }
    }
}
