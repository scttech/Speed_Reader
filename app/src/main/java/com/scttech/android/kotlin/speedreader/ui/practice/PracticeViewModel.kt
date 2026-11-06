package com.scttech.android.kotlin.speedreader.ui.practice

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.speedreader.data.PassageRepository
import com.scttech.android.kotlin.speedreader.data.SessionRepository
import com.scttech.android.kotlin.speedreader.data.SettingsRepository
import com.scttech.android.kotlin.speedreader.data.db.ReadingSessionEntity
import com.scttech.android.kotlin.speedreader.model.Passage
import com.scttech.android.kotlin.speedreader.model.TargetChange
import com.scttech.android.kotlin.speedreader.model.comprehension
import com.scttech.android.kotlin.speedreader.model.effectiveWpm
import com.scttech.android.kotlin.speedreader.model.initialTargetWpm
import com.scttech.android.kotlin.speedreader.model.isPlausibleRawWpm
import com.scttech.android.kotlin.speedreader.model.nextTarget
import com.scttech.android.kotlin.speedreader.model.rawWpm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

const val SESSION_TYPE_PRACTICE = "practice"

sealed interface PracticeUiState {
    data object Loading : PracticeUiState
    data class Intro(val passage: Passage, val targetWpm: Int) : PracticeUiState
    data class Reading(val passage: Passage, val targetWpm: Int) : PracticeUiState
    data class Quiz(val passage: Passage, val targetWpm: Int, val index: Int) : PracticeUiState
    data class TooFast(val rawWpm: Int) : PracticeUiState
    data object Saving : PracticeUiState
    data class Result(
        val rawWpm: Int,
        val correct: Int,
        val total: Int,
        val effectiveWpm: Int,
        val targetWpm: Int,
        val nextTargetWpm: Int,
        val change: TargetChange,
    ) : PracticeUiState
}

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val passages: PassageRepository,
    private val sessions: SessionRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PracticeUiState>(PracticeUiState.Loading)
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    private var startedAt = 0L
    private var measuredRawWpm = 0
    private val answers = mutableListOf<Int>()

    init {
        loadIntro()
    }

    private fun loadIntro() {
        _uiState.value = PracticeUiState.Loading
        viewModelScope.launch {
            val attempt = sessions.countOfType(SESSION_TYPE_PRACTICE)
            val passage = passages.practicePassage(attempt)
            val target = settings.practiceTargetWpm.first()
                ?: initialTargetWpm(sessions.latestEffectiveWpm.first())
            _uiState.value = PracticeUiState.Intro(passage, target)
        }
    }

    fun start() {
        val s = _uiState.value as? PracticeUiState.Intro ?: return
        answers.clear()
        startedAt = SystemClock.elapsedRealtime()
        _uiState.value = PracticeUiState.Reading(s.passage, s.targetWpm)
    }

    fun finishReading() {
        val s = _uiState.value as? PracticeUiState.Reading ?: return
        val elapsed = SystemClock.elapsedRealtime() - startedAt
        measuredRawWpm = rawWpm(s.passage.wordCount, elapsed)
        if (!isPlausibleRawWpm(measuredRawWpm)) {
            _uiState.value = PracticeUiState.TooFast(measuredRawWpm)   // not recorded, target unchanged
            return
        }
        _uiState.value = PracticeUiState.Quiz(s.passage, s.targetWpm, index = 0)
    }

    fun answer(optionIndex: Int) {
        val s = _uiState.value as? PracticeUiState.Quiz ?: return
        answers += optionIndex
        if (s.index + 1 < s.passage.questions.size) {
            _uiState.value = s.copy(index = s.index + 1)
        } else {
            finish(s.passage, s.targetWpm)
        }
    }

    fun nextExcerpt() = loadIntro()

    private fun finish(passage: Passage, targetWpm: Int) {
        _uiState.value = PracticeUiState.Saving
        viewModelScope.launch {
            val total = passage.questions.size
            val correct = passage.questions.indices.count { answers[it] == passage.questions[it].correctIndex }
            val comp = comprehension(correct, total)
            val effective = effectiveWpm(measuredRawWpm, comp)
            val adjustment = nextTarget(
                currentTargetWpm = targetWpm,
                comprehension = comp,
                raisePercent = settings.raisePercent.first(),
                lowerPercent = settings.lowerPercent.first(),
            )
            sessions.record(
                ReadingSessionEntity(
                    timestampMillis = System.currentTimeMillis(),
                    type = SESSION_TYPE_PRACTICE,
                    rawWpm = measuredRawWpm,
                    comprehension = comp,
                    effectiveWpm = effective,
                ),
            )
            settings.setPracticeTargetWpm(adjustment.nextTargetWpm)
            _uiState.value = PracticeUiState.Result(
                rawWpm = measuredRawWpm,
                correct = correct,
                total = total,
                effectiveWpm = effective,
                targetWpm = targetWpm,
                nextTargetWpm = adjustment.nextTargetWpm,
                change = adjustment.change,
            )
        }
    }
}
