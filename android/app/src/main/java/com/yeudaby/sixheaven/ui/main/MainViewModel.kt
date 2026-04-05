package com.yeudaby.sixheaven.ui.main

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yeudaby.sixheaven.data.model.KashrutState
import com.yeudaby.sixheaven.data.model.KashrutTimerState
import com.yeudaby.sixheaven.data.model.WaitSettings
import com.yeudaby.sixheaven.data.repository.KashrutRepository
import com.yeudaby.sixheaven.receiver.AlarmReceiver
import com.yeudaby.sixheaven.service.KashrutCountdownForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val timerState: KashrutTimerState = KashrutTimerState(),
    val settings: WaitSettings = WaitSettings(),
    /** Snapshot refreshed every second so the UI recomposes. */
    val remainingMs: Long = 0L
)

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: KashrutRepository,
    private val alarmManager: AlarmManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeRepository()
        startTicker()
    }

    private fun observeRepository() {
        viewModelScope.launch {
            combine(repository.timerState, repository.waitSettings) { timer, settings ->
                MainUiState(
                    timerState = timer,
                    settings = settings,
                    remainingMs = timer.remainingMs
                )
            }.collect { _uiState.value = it }
        }
    }

    /** Refreshes remainingMs every second so the countdown recomposes. */
    private fun startTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val state = _uiState.value
                if (state.timerState.isActive) {
                    val remaining = state.timerState.remainingMs
                    _uiState.update { it.copy(remainingMs = remaining) }
                    if (remaining == 0L) {
                        // Fallback: AlarmManager should handle this, but reset just in case
                        repository.cancelTimer()
                        stopCountdownService()
                    }
                }
            }
        }
    }

    // ── User actions ──────────────────────────────────────────────────────────

    fun ateMeat() = viewModelScope.launch {
        val settings = _uiState.value.settings
        repository.startMeatTimer(settings)
        val endTimeMs = System.currentTimeMillis() + settings.meatWaitMinutes * 60_000L
        scheduleAlarm(KashrutState.MEATY, endTimeMs)
        startCountdownService()
    }

    fun ateDairy() = viewModelScope.launch {
        val settings = _uiState.value.settings
        repository.startDairyTimer(settings)
        val endTimeMs = System.currentTimeMillis() + settings.dairyWaitMinutes * 60_000L
        scheduleAlarm(KashrutState.DAIRY, endTimeMs)
        startCountdownService()
    }

    fun cancelTimer() = viewModelScope.launch {
        repository.cancelTimer()
        cancelAlarm()
        stopCountdownService()
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun scheduleAlarm(forState: KashrutState, triggerAtMs: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_STATE, forState.name)
        }
        val pi = PendingIntent.getBroadcast(
            context, ALARM_RC, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
    }

    private fun cancelAlarm() {
        val pi = PendingIntent.getBroadcast(
            context, ALARM_RC,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pi?.let { alarmManager.cancel(it) }
    }

    private fun startCountdownService() {
        context.startForegroundService(
            Intent(context, KashrutCountdownForegroundService::class.java)
                .setAction(KashrutCountdownForegroundService.ACTION_START)
        )
    }

    private fun stopCountdownService() {
        context.startService(
            Intent(context, KashrutCountdownForegroundService::class.java)
                .setAction(KashrutCountdownForegroundService.ACTION_STOP)
        )
    }

    companion object {
        private const val ALARM_RC = 2001
    }
}
