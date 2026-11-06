package com.scttech.android.kotlin.speedreader.data

import com.scttech.android.kotlin.speedreader.data.db.DrillResultDao
import com.scttech.android.kotlin.speedreader.data.db.DrillResultEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface DrillResultRepository {
    /** Newest first. */
    val results: Flow<List<DrillResultEntity>>
    suspend fun record(drill: String, value: Int, detail: Int, timestampMillis: Long = System.currentTimeMillis())
    suspend fun countOf(drill: String): Int
}

@Singleton
class RoomDrillResultRepository @Inject constructor(
    private val dao: DrillResultDao,
) : DrillResultRepository {
    override val results: Flow<List<DrillResultEntity>> = dao.observeAll()

    override suspend fun record(drill: String, value: Int, detail: Int, timestampMillis: Long) {
        dao.insert(DrillResultEntity(timestampMillis = timestampMillis, drill = drill, value = value, detail = detail))
    }

    override suspend fun countOf(drill: String): Int = dao.countOf(drill)
}
