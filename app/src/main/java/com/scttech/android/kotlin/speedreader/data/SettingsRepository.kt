package com.scttech.android.kotlin.speedreader.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.scttech.android.kotlin.speedreader.model.DEFAULT_ADJUST_PERCENT
import com.scttech.android.kotlin.speedreader.model.DEFAULT_GOAL_WPM
import com.scttech.android.kotlin.speedreader.model.clampAdjustPercent
import com.scttech.android.kotlin.speedreader.model.clampGoal
import com.scttech.android.kotlin.speedreader.reminder.DEFAULT_REMINDER_MINUTE_OF_DAY
import com.scttech.android.kotlin.speedreader.reminder.MINUTES_PER_DAY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface SettingsRepository {
    /** User's goal in effective WPM; defaults to [DEFAULT_GOAL_WPM]. */
    val goalWpm: Flow<Int>
    suspend fun setGoalWpm(wpm: Int)

    /** How much (percent) the practice target goes up after a strong result. */
    val raisePercent: Flow<Int>
    suspend fun setRaisePercent(percent: Int)

    /** How much (percent) the practice target goes down after a weak result. */
    val lowerPercent: Flow<Int>
    suspend fun setLowerPercent(percent: Int)

    /** Daily reminder: off by default, at [DEFAULT_REMINDER_MINUTE_OF_DAY] unless the user picks another time. */
    val reminderEnabled: Flow<Boolean>
    suspend fun setReminderEnabled(enabled: Boolean)
    val reminderMinuteOfDay: Flow<Int>
    suspend fun setReminderMinuteOfDay(minuteOfDay: Int)

    /** Current practice target, or null until the first practice (it is then derived from the latest session). */
    val practiceTargetWpm: Flow<Int?>
    suspend fun setPracticeTargetWpm(wpm: Int)
    suspend fun clearPracticeTarget()
}

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val goalWpm: Flow<Int> =
        dataStore.data.map { it[GOAL_KEY]?.let(::clampGoal) ?: DEFAULT_GOAL_WPM }

    override suspend fun setGoalWpm(wpm: Int) {
        dataStore.edit { it[GOAL_KEY] = clampGoal(wpm) }
    }

    override val raisePercent: Flow<Int> =
        dataStore.data.map { it[RAISE_KEY] ?: DEFAULT_ADJUST_PERCENT }

    override suspend fun setRaisePercent(percent: Int) {
        dataStore.edit { it[RAISE_KEY] = clampAdjustPercent(percent) }
    }

    override val lowerPercent: Flow<Int> =
        dataStore.data.map { it[LOWER_KEY] ?: DEFAULT_ADJUST_PERCENT }

    override suspend fun setLowerPercent(percent: Int) {
        dataStore.edit { it[LOWER_KEY] = clampAdjustPercent(percent) }
    }

    override val reminderEnabled: Flow<Boolean> = dataStore.data.map { it[REMINDER_ON_KEY] ?: false }

    override suspend fun setReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[REMINDER_ON_KEY] = enabled }
    }

    override val reminderMinuteOfDay: Flow<Int> =
        dataStore.data.map { it[REMINDER_TIME_KEY] ?: DEFAULT_REMINDER_MINUTE_OF_DAY }

    override suspend fun setReminderMinuteOfDay(minuteOfDay: Int) {
        dataStore.edit { it[REMINDER_TIME_KEY] = minuteOfDay.coerceIn(0, MINUTES_PER_DAY - 1) }
    }

    override val practiceTargetWpm: Flow<Int?> =
        dataStore.data.map { it[TARGET_KEY] }

    override suspend fun setPracticeTargetWpm(wpm: Int) {
        dataStore.edit { it[TARGET_KEY] = wpm }
    }

    override suspend fun clearPracticeTarget() {
        dataStore.edit { it.remove(TARGET_KEY) }
    }

    private companion object {
        val GOAL_KEY = intPreferencesKey("goal_wpm")
        val RAISE_KEY = intPreferencesKey("raise_percent")
        val LOWER_KEY = intPreferencesKey("lower_percent")
        val TARGET_KEY = intPreferencesKey("practice_target_wpm")
        val REMINDER_ON_KEY = booleanPreferencesKey("reminder_enabled")
        val REMINDER_TIME_KEY = intPreferencesKey("reminder_minute_of_day")
    }
}
