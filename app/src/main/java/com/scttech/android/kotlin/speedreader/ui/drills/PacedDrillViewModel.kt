package com.scttech.android.kotlin.speedreader.ui.drills

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scttech.android.kotlin.speedreader.data.DrillSpeedProvider
import com.scttech.android.kotlin.speedreader.data.DrillResultRepository
import com.scttech.android.kotlin.speedreader.data.PassageLibrary
import com.scttech.android.kotlin.speedreader.model.GuidedLine
import com.scttech.android.kotlin.speedreader.model.MAX_TARGET_WPM
import com.scttech.android.kotlin.speedreader.model.MIN_TARGET_WPM
import com.scttech.android.kotlin.speedreader.model.columnLines
import com.scttech.android.kotlin.speedreader.model.corpusWords
import com.scttech.android.kotlin.speedreader.model.twoStopLines
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

enum class PacedDrillKind(
    val key: String,
    val title: String,
    val instructions: String,
    val durationSeconds: Int,
    val ramps: Boolean,
) {
    COLUMN(
        "column", "Column drill",
        "Keep your eyes on the centre line and let them move down only. Take in each whole line in one glance, " +
            "following the orange highlight.",
        60, false,
    ),
    TWO_STOP(
        "twostop", "Two-stop lines",
        "Each line has two stops. Look at the middle of the highlighted chunk, then jump straight to the next " +
            "one — never word-by-word.",
        90, false,
    ),
    METRONOME(
        "metronome", "Metronome ramp",
        "Follow the beat as it speeds up by $RAMP_LABEL. Don't worry about every word — keep up and let your " +
            "brain adjust to the faster pace. Tap Stop when you lose it.",
        180, true,
    );

    companion object {
        fun fromKey(key: String?): PacedDrillKind = entries.firstOrNull { it.key == key } ?: COLUMN
    }
}

private const val MIN_RECORDED_SECONDS = 10
private const val RAMP_LABEL = "25 WPM every 15 seconds"

sealed interface PacedDrillUiState {
    data object Loading : PacedDrillUiState
    data class Intro(val kind: PacedDrillKind, val startWpm: Int) : PacedDrillUiState
    data class Running(val kind: PacedDrillKind, val startWpm: Int, val lines: List<GuidedLine>) : PacedDrillUiState
    data class Finished(val kind: PacedDrillKind, val startWpm: Int, val endWpm: Int, val seconds: Int) : PacedDrillUiState
}

@HiltViewModel
class PacedDrillViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val speed: DrillSpeedProvider,
    private val results: DrillResultRepository,
) : ViewModel() {

    val kind: PacedDrillKind = PacedDrillKind.fromKey(savedState.get<String>("kind"))

    private val _uiState = MutableStateFlow<PacedDrillUiState>(PacedDrillUiState.Loading)
    val uiState: StateFlow<PacedDrillUiState> = _uiState.asStateFlow()

    private val words = corpusWords(PassageLibrary.baseline.map { it.text })

    init {
        viewModelScope.launch {
            _uiState.value = PacedDrillUiState.Intro(kind, speed.startWpm())
        }
    }

    /** Adjusts the starting speed on the intro screen. */
    fun adjustSpeed(deltaWpm: Int) {
        val s = _uiState.value as? PacedDrillUiState.Intro ?: return
        _uiState.value = s.copy(startWpm = (s.startWpm + deltaWpm).coerceIn(MIN_TARGET_WPM, MAX_TARGET_WPM))
    }

    fun start() {
        val s = _uiState.value as? PacedDrillUiState.Intro ?: return
        val offset = Random.nextInt(words.size)
        val text = (words.drop(offset) + words.take(offset)).let { it + it }   // enough text for the longest drill
        val lines = if (kind == PacedDrillKind.COLUMN) columnLines(text) else twoStopLines(text)
        _uiState.value = PacedDrillUiState.Running(kind, s.startWpm, lines)
    }

    fun finish(endWpm: Int, seconds: Int) {
        val s = _uiState.value as? PacedDrillUiState.Running ?: return
        _uiState.value = PacedDrillUiState.Finished(kind, s.startWpm, endWpm, seconds)
        if (seconds >= MIN_RECORDED_SECONDS) {            // ignore accidental instant stops
            viewModelScope.launch {
                results.record(kind.key, value = if (kind.ramps) endWpm else s.startWpm, detail = if (kind.ramps) s.startWpm else seconds)
            }
        }
    }

    fun again() {
        val s = _uiState.value as? PacedDrillUiState.Finished ?: return
        _uiState.value = PacedDrillUiState.Intro(kind, s.startWpm)
    }
}
