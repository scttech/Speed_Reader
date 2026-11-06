package com.scttech.android.kotlin.speedreader.data

import com.scttech.android.kotlin.speedreader.model.initialTargetWpm
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** Starting speed for pacing drills: the user's current practice target (or one derived from their latest session). */
@Singleton
class DrillSpeedProvider @Inject constructor(
    private val settings: SettingsRepository,
    private val sessions: SessionRepository,
) {
    suspend fun startWpm(): Int =
        settings.practiceTargetWpm.first() ?: initialTargetWpm(sessions.latestEffectiveWpm.first())
}
