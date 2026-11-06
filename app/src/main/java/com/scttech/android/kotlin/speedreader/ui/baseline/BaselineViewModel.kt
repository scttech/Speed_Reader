package com.scttech.android.kotlin.speedreader.ui.baseline

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.speedreader.data.PassageRepository
import com.scttech.android.kotlin.speedreader.data.SessionRepository
import com.scttech.android.kotlin.speedreader.data.SettingsRepository
import com.scttech.android.kotlin.speedreader.data.db.ReadingSessionEntity
import com.scttech.android.kotlin.speedreader.model.Passage
import com.scttech.android.kotlin.speedreader.model.comprehension
import com.scttech.android.kotlin.speedreader.model.effectiveWpm
import com.scttech.android.kotlin.speedreader.model.gaugeLevel
import com.scttech.android.kotlin.speedreader.model.isPlausibleRawWpm
import com.scttech.android.kotlin.speedreader.model.rawWpm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

const val SESSION_TYPE_BASELINE = "baseline"

sealed interface BaselineUiState {
    data object Intro : BaselineUiState
    data class Reading(val passage: Passage) : BaselineUiState
    data class Quiz(val passage: Passage, val index: Int) : BaselineUiState
    data class TooFast(val rawWpm: Int) : BaselineUiState
    data object Saving : BaselineUiState
    data class Result(
        val rawWpm: Int,
        val correct: Int,
        val total: Int,
        val effectiveWpm: Int,
        val level: Int,
        val goalWpm: Int,
    ) : BaselineUiState
}

@HiltViewModel
class BaselineViewModel @Inject constructor(
    private val passages: PassageRepository,
    private val sessions: SessionRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BaselineUiState>(BaselineUiState.Intro)
    val uiState: StateFlow<BaselineUiState> = _uiState.asStateFlow()

    private var startedAt = 0L
    private var measuredRawWpm = 0
    private val answers = mutableListOf<Int>()

    fun start() {
        viewModelScope.launch {
            val attempt = sessions.countOfType(SESSION_TYPE_BASELINE)
            val passage = passages.baselinePassage(attempt)
            answers.clear()
            startedAt = SystemClock.elapsedRealtime()
            _uiState.value = BaselineUiState.Reading(passage)
        }
    }

    fun finishReading() {
        val state = _uiState.value as? BaselineUiState.Reading ?: return
        val elapsed = SystemClock.elapsedRealtime() - startedAt
        measuredRawWpm = rawWpm(state.passage.wordCount, elapsed)
        if (!isPlausibleRawWpm(measuredRawWpm)) {
            _uiState.value = BaselineUiState.TooFast(measuredRawWpm)   // not recorded
            return
        }
        _uiState.value = BaselineUiState.Quiz(state.passage, index = 0)
    }

    fun answer(optionIndex: Int) {
        val state = _uiState.value as? BaselineUiState.Quiz ?: return
        answers += optionIndex
        if (state.index + 1 < state.passage.questions.size) {
            _uiState.value = state.copy(index = state.index + 1)
        } else {
            finish(state.passage)
        }
    }

    fun restart() {
        _uiState.value = BaselineUiState.Intro
    }

    private fun finish(passage: Passage) {
        _uiState.value = BaselineUiState.Saving
        viewModelScope.launch {
            val total = passage.questions.size
            val correct = passage.questions.indices.count { answers[it] == passage.questions[it].correctIndex }
            val comp = comprehension(correct, total)
            val effective = effectiveWpm(measuredRawWpm, comp)
            val goal = settings.goalWpm.first()
            sessions.record(
                ReadingSessionEntity(
                    timestampMillis = System.currentTimeMillis(),
                    type = SESSION_TYPE_BASELINE,
                    rawWpm = measuredRawWpm,
                    comprehension = comp,
                    effectiveWpm = effective,
                ),
            )
            settings.clearPracticeTarget()   // practice target is re-derived from this new baseline
            _uiState.value = BaselineUiState.Result(
                rawWpm = measuredRawWpm,
                correct = correct,
                total = total,
                effectiveWpm = effective,
                level = gaugeLevel(effective, goal),
                goalWpm = goal,
            )
        }
    }
}
