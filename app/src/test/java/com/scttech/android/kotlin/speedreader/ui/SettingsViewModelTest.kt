package com.scttech.android.kotlin.speedreader.ui

import com.scttech.android.kotlin.speedreader.data.SessionRepository
import com.scttech.android.kotlin.speedreader.data.SettingsRepository
import com.scttech.android.kotlin.speedreader.data.db.ReadingSessionEntity
import com.scttech.android.kotlin.speedreader.reminder.ReminderController
import com.scttech.android.kotlin.speedreader.ui.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private class FakeSettings : SettingsRepository {
        override val goalWpm = MutableStateFlow(600)
        override suspend fun setGoalWpm(wpm: Int) { goalWpm.value = wpm }
        override val raisePercent = MutableStateFlow(10)
        override suspend fun setRaisePercent(percent: Int) { raisePercent.value = percent }
        override val lowerPercent = MutableStateFlow(10)
        override suspend fun setLowerPercent(percent: Int) { lowerPercent.value = percent }
        override val reminderEnabled = MutableStateFlow(false)
        override suspend fun setReminderEnabled(enabled: Boolean) { reminderEnabled.value = enabled }
        override val reminderMinuteOfDay = MutableStateFlow(19 * 60)
        override suspend fun setReminderMinuteOfDay(minuteOfDay: Int) { reminderMinuteOfDay.value = minuteOfDay }
        override val practiceTargetWpm = MutableStateFlow<Int?>(300)
        override suspend fun setPracticeTargetWpm(wpm: Int) { practiceTargetWpm.value = wpm }
        override suspend fun clearPracticeTarget() { practiceTargetWpm.value = null }
    }

    private class FakeSessions(initial: List<ReadingSessionEntity>) : SessionRepository {
        val all = MutableStateFlow(initial)
        override val latestEffectiveWpm: Flow<Int> = MutableStateFlow(0)
        override val sessions: Flow<List<ReadingSessionEntity>> = all
        override suspend fun record(session: ReadingSessionEntity) { all.value = all.value + session }
        override suspend fun countOfType(type: String) = all.value.count { it.type == type }
        override suspend fun clearAll() { all.value = emptyList() }
    }

    private class FakeReminder : ReminderController {
        val calls = mutableListOf<String>()
        override fun schedule(minuteOfDay: Int) { calls += "schedule $minuteOfDay" }
        override fun cancel() { calls += "cancel" }
    }

    private val session = ReadingSessionEntity(timestampMillis = 1, type = "practice", rawWpm = 300, comprehension = 0.8f, effectiveWpm = 240)
    private lateinit var settings: FakeSettings
    private lateinit var sessions: FakeSessions
    private lateinit var reminder: FakeReminder
    private lateinit var vm: SettingsViewModel

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        settings = FakeSettings(); sessions = FakeSessions(listOf(session)); reminder = FakeReminder()
        vm = SettingsViewModel(settings, sessions, reminder)
    }

    @After fun tearDown() = Dispatchers.resetMain()

    private fun collectCount(): Job =
        CoroutineScope(UnconfinedTestDispatcher()).launch { vm.resettableCount.collect { } }

    @Test fun confirmWithoutDialogDoesNothing() {
        val job = collectCount()
        vm.confirmReset()
        assertEquals(1, sessions.all.value.size)
        assertEquals(300, settings.practiceTargetWpm.value)
        job.cancel()
    }

    @Test fun cancelKeepsData() {
        val job = collectCount()
        vm.requestReset(); assertTrue(vm.resetConfirmVisible.value)
        vm.cancelReset()
        assertFalse(vm.resetConfirmVisible.value)
        assertEquals(1, sessions.all.value.size)
        job.cancel()
    }

    @Test fun confirmAfterDialogClearsSessionsAndTarget() {
        val job = collectCount()
        vm.requestReset()
        vm.confirmReset()
        assertTrue(sessions.all.value.isEmpty())
        assertNull(settings.practiceTargetWpm.value)
        assertEquals(600, settings.goalWpm.value)
        assertFalse(vm.resetConfirmVisible.value)
        job.cancel()
    }

    @Test fun requestIgnoredWhenNothingToReset() {
        sessions.all.value = emptyList()
        val job = collectCount()
        vm.requestReset()
        assertFalse(vm.resetConfirmVisible.value)
        job.cancel()
    }

    @Test fun reminderToggleSchedulesAndCancels() {
        vm.setReminderEnabled(true)
        vm.setReminderTime(8 * 60)
        vm.setReminderEnabled(false)
        vm.setReminderTime(9 * 60)           // disabled: must not reschedule
        assertEquals(listOf("schedule 1140", "schedule 480", "cancel"), reminder.calls)
    }
}
