package com.scttech.android.kotlin.speedreader.ui.drills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.speedreader.data.DrillResultRepository
import com.scttech.android.kotlin.speedreader.data.PassageLibrary
import com.scttech.android.kotlin.speedreader.data.db.DrillKind
import com.scttech.android.kotlin.speedreader.model.SPAN_FLASH_MILLIS
import com.scttech.android.kotlin.speedreader.model.SPAN_ROUNDS
import com.scttech.android.kotlin.speedreader.model.SpanLevel
import com.scttech.android.kotlin.speedreader.model.SpanQuestion
import com.scttech.android.kotlin.speedreader.model.makeSpanQuestion
import com.scttech.android.kotlin.speedreader.model.nextSpanLevel
import com.scttech.android.kotlin.speedreader.model.splitSentences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

sealed interface SpanDrillUiState {
    data object Intro : SpanDrillUiState
    data class Fixation(val round: Int) : SpanDrillUiState
    data class Flash(val phrase: String, val round: Int) : SpanDrillUiState
    data class Choose(val question: SpanQuestion, val round: Int) : SpanDrillUiState
    data class Feedback(val correct: Boolean, val phrase: String, val round: Int) : SpanDrillUiState
    data class Summary(val widestRead: Int, val correct: Int, val total: Int) : SpanDrillUiState
}

@HiltViewModel
class SpanDrillViewModel @Inject constructor(
    private val results: DrillResultRepository,
) : ViewModel() {

    private val sentences = splitSentences(PassageLibrary.baseline.map { it.text })
    private val _uiState = MutableStateFlow<SpanDrillUiState>(SpanDrillUiState.Intro)
    val uiState: StateFlow<SpanDrillUiState> = _uiState.asStateFlow()

    private var level = SpanLevel()
    private var round = 0
    private var correctCount = 0
    private var widestRead = 0
    private var pending: SpanQuestion? = null
    private var job: Job? = null

    fun start() {
        level = SpanLevel(); round = 0; correctCount = 0; widestRead = 0
        nextRound()
    }

    private fun nextRound() {
        job?.cancel()
        job = viewModelScope.launch {
            round++
            _uiState.value = SpanDrillUiState.Fixation(round)
            delay(700)
            val q = makeSpanQuestion(sentences, level.width, Random.Default)
            _uiState.value = SpanDrillUiState.Flash(q.phrase, round)
            delay(SPAN_FLASH_MILLIS)
            pending = q
            _uiState.value = SpanDrillUiState.Choose(q, round)
        }
    }

    fun answer(optionIndex: Int) {
        val q = pending ?: return
        pending = null
        val ok = optionIndex == q.correctIndex
        if (ok) {
            correctCount++
            widestRead = maxOf(widestRead, q.phrase.split(" ").size)
        }
        level = nextSpanLevel(level, ok)
        _uiState.value = SpanDrillUiState.Feedback(ok, q.phrase, round)
        job?.cancel()
        job = viewModelScope.launch {
            delay(900)
            if (round >= SPAN_ROUNDS) {
                results.record(DrillKind.SPAN, value = widestRead, detail = correctCount)
                _uiState.value = SpanDrillUiState.Summary(widestRead, correctCount, SPAN_ROUNDS)
            } else nextRound()
        }
    }

    fun restart() {
        job?.cancel(); pending = null
        _uiState.value = SpanDrillUiState.Intro
    }
}
