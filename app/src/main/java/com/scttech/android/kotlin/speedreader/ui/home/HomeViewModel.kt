package com.scttech.android.kotlin.speedreader.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.speedreader.data.SessionRepository
import com.scttech.android.kotlin.speedreader.data.SettingsRepository
import com.scttech.android.kotlin.speedreader.model.DEFAULT_GOAL_WPM
import com.scttech.android.kotlin.speedreader.model.gaugeLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val effectiveWpm: Int = 0,
    val goalWpm: Int = DEFAULT_GOAL_WPM,
    val level: Int = 0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    sessions: SessionRepository,
    settings: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        combine(sessions.latestEffectiveWpm, settings.goalWpm) { wpm, goal ->
            HomeUiState(effectiveWpm = wpm, goalWpm = goal, level = gaugeLevel(wpm, goal))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )
}
