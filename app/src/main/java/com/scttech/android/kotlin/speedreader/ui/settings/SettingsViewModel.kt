package com.scttech.android.kotlin.speedreader.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.speedreader.data.SessionRepository
import com.scttech.android.kotlin.speedreader.data.SettingsRepository
import com.scttech.android.kotlin.speedreader.model.DEFAULT_ADJUST_PERCENT
import com.scttech.android.kotlin.speedreader.model.DEFAULT_GOAL_WPM
import com.scttech.android.kotlin.speedreader.reminder.DEFAULT_REMINDER_MINUTE_OF_DAY
import com.scttech.android.kotlin.speedreader.reminder.ReminderController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val sessions: SessionRepository,
    private val reminder: ReminderController,
) : ViewModel() {

    private fun <T> Flow<T>.state(initial: T) = stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

    val goalWpm: StateFlow<Int> = settings.goalWpm.state(DEFAULT_GOAL_WPM)
    val raisePercent: StateFlow<Int> = settings.raisePercent.state(DEFAULT_ADJUST_PERCENT)
    val lowerPercent: StateFlow<Int> = settings.lowerPercent.state(DEFAULT_ADJUST_PERCENT)
    val reminderEnabled: StateFlow<Boolean> = settings.reminderEnabled.state(false)
    val reminderMinuteOfDay: StateFlow<Int> = settings.reminderMinuteOfDay.state(DEFAULT_REMINDER_MINUTE_OF_DAY)

    /** How many baseline and practice results a reset would delete. */
    val resettableCount: StateFlow<Int> = sessions.sessions.map { it.size }.state(0)

    private val _resetConfirmVisible = MutableStateFlow(false)
    val resetConfirmVisible: StateFlow<Boolean> = _resetConfirmVisible.asStateFlow()

    fun setGoal(wpm: Int) { viewModelScope.launch { settings.setGoalWpm(wpm) } }
    fun setRaisePercent(percent: Int) { viewModelScope.launch { settings.setRaisePercent(percent) } }
    fun setLowerPercent(percent: Int) { viewModelScope.launch { settings.setLowerPercent(percent) } }

    // ---- daily reminder ----
    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settings.setReminderEnabled(enabled)
            if (enabled) reminder.schedule(settings.reminderMinuteOfDay.first()) else reminder.cancel()
        }
    }

    fun setReminderTime(minuteOfDay: Int) {
        viewModelScope.launch {
            settings.setReminderMinuteOfDay(minuteOfDay)
            if (settings.reminderEnabled.first()) reminder.schedule(settings.reminderMinuteOfDay.first())
        }
    }

    // ---- reset effective WPM (always goes through a confirmation) ----
    fun requestReset() { if (resettableCount.value > 0) _resetConfirmVisible.value = true }

    fun cancelReset() { _resetConfirmVisible.value = false }

    /** Deletes all baseline and practice results and the practice target. Drill results and settings are kept. */
    fun confirmReset() {
        if (!_resetConfirmVisible.value) return            // can only run after the dialog was shown
        _resetConfirmVisible.value = false
        viewModelScope.launch {
            sessions.clearAll()
            settings.clearPracticeTarget()
        }
    }
}
