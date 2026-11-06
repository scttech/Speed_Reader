package com.scttech.android.kotlin.speedreader.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.speedreader.data.DrillResultRepository
import com.scttech.android.kotlin.speedreader.data.SessionRepository
import com.scttech.android.kotlin.speedreader.data.SettingsRepository
import com.scttech.android.kotlin.speedreader.progress.ProgressSummary
import com.scttech.android.kotlin.speedreader.progress.buildProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    sessions: SessionRepository,
    drills: DrillResultRepository,
    settings: SettingsRepository,
) : ViewModel() {

    /** Null until the first emission from the database. */
    val summary: StateFlow<ProgressSummary?> =
        combine(sessions.sessions, drills.results, settings.goalWpm) { s, d, goal ->
            val zone = ZoneId.systemDefault()
            buildProgress(s, d, goal, LocalDate.now(zone).toEpochDay(), zone)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
