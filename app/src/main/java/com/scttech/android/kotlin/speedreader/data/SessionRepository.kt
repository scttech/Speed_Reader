package com.scttech.android.kotlin.speedreader.data

import com.scttech.android.kotlin.speedreader.data.db.ReadingSessionDao
import com.scttech.android.kotlin.speedreader.data.db.ReadingSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface SessionRepository {
    /** Effective WPM of the most recent session, or 0 if none yet. */
    val latestEffectiveWpm: Flow<Int>

    /** All sessions, newest first. */
    val sessions: Flow<List<ReadingSessionEntity>>
    suspend fun record(session: ReadingSessionEntity)
    suspend fun countOfType(type: String): Int

    /** Deletes every baseline and practice result (drill results are separate and untouched). */
    suspend fun clearAll()
}

@Singleton
class RoomSessionRepository @Inject constructor(
    private val dao: ReadingSessionDao,
) : SessionRepository {
    override val latestEffectiveWpm: Flow<Int> =
        dao.observeLatest().map { it?.effectiveWpm ?: 0 }

    override val sessions: Flow<List<ReadingSessionEntity>> = dao.observeAll()

    override suspend fun record(session: ReadingSessionEntity) {
        dao.insert(session)
    }

    override suspend fun countOfType(type: String): Int = dao.countOfType(type)

    override suspend fun clearAll() = dao.deleteAll()
}
