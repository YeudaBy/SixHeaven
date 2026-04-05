package com.yeudaby.sixheaven.data.repository

import com.yeudaby.sixheaven.data.datastore.KashrutDataStore
import com.yeudaby.sixheaven.data.model.KashrutState
import com.yeudaby.sixheaven.data.model.KashrutTimerState
import com.yeudaby.sixheaven.data.model.WaitSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KashrutRepository @Inject constructor(
    private val dataStore: KashrutDataStore
) {
    val timerState: Flow<KashrutTimerState> = dataStore.timerState
    val waitSettings: Flow<WaitSettings> = dataStore.waitSettings

    suspend fun startMeatTimer(settings: WaitSettings) {
        val now = System.currentTimeMillis()
        dataStore.setTimerState(
            KashrutTimerState(
                state = KashrutState.MEATY,
                startTimeMs = now,
                totalWaitMs = settings.meatWaitMinutes * 60 * 1000L
            )
        )
    }

    suspend fun startDairyTimer(settings: WaitSettings) {
        val now = System.currentTimeMillis()
        dataStore.setTimerState(
            KashrutTimerState(
                state = KashrutState.DAIRY,
                startTimeMs = now,
                totalWaitMs = settings.dairyWaitMinutes * 60 * 1000L
            )
        )
    }

    suspend fun cancelTimer() = dataStore.resetTimer()

    suspend fun updateSettings(settings: WaitSettings) = dataStore.setWaitSettings(settings)
}
