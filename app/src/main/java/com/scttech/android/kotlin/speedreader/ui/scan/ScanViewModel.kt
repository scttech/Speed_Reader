package com.scttech.android.kotlin.speedreader.ui.scan

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.speedreader.data.DrillResultRepository
import com.scttech.android.kotlin.speedreader.data.PassageRepository
import com.scttech.android.kotlin.speedreader.data.db.DrillKind
import com.scttech.android.kotlin.speedreader.model.ScanTask
import com.scttech.android.kotlin.speedreader.model.scanWpm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ScanUiState {
    data object Intro : ScanUiState
    data class Scanning(val task: ScanTask) : ScanUiState
    data class Answering(val task: ScanTask, val seconds: Int) : ScanUiState
    data class Review(val result: Result) : ScanUiState
    data class Result(
        val task: ScanTask,
        val chosen: Int,
        val correct: Boolean,
        val seconds: Int,
        val scanWpm: Int,
    ) : ScanUiState
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val passages: PassageRepository,
    private val results: DrillResultRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Intro)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private var startedAt = 0L

    fun start() {
        viewModelScope.launch {
            val task = passages.scanTask(results.countOf(DrillKind.SCAN))
            startedAt = SystemClock.elapsedRealtime()
            _uiState.value = ScanUiState.Scanning(task)
        }
    }

    /** The reader says they have spotted the answer; stop the clock and ask for it. */
    fun found() {
        val s = _uiState.value as? ScanUiState.Scanning ?: return
        val seconds = ((SystemClock.elapsedRealtime() - startedAt) / 1000).toInt().coerceAtLeast(1)
        _uiState.value = ScanUiState.Answering(s.task, seconds)
    }

    fun answer(option: Int) {
        val s = _uiState.value as? ScanUiState.Answering ?: return
        val correct = option == s.task.question.correctIndex
        val wpm = scanWpm(s.task.passage.wordCount, s.seconds, correct)
        _uiState.value = ScanUiState.Result(s.task, option, correct, s.seconds, wpm)
        viewModelScope.launch { results.record(DrillKind.SCAN, value = wpm, detail = if (correct) 1 else 0) }
    }

    fun again() = start()

    /** Show the passage with the answer's sentence highlighted. */
    fun review() {
        val r = _uiState.value as? ScanUiState.Result ?: return
        _uiState.value = ScanUiState.Review(r)
    }

    fun closeReview() {
        val r = _uiState.value as? ScanUiState.Review ?: return
        _uiState.value = r.result
    }

    fun reset() {
        _uiState.value = ScanUiState.Intro
    }
}
