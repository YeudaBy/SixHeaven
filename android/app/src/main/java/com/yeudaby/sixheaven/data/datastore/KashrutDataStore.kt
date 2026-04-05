package com.yeudaby.sixheaven.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.yeudaby.sixheaven.data.model.KashrutState
import com.yeudaby.sixheaven.data.model.KashrutTimerState
import com.yeudaby.sixheaven.data.model.WaitSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kashrut_prefs")

@Singleton
class KashrutDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val STATE = stringPreferencesKey("kashrut_state")
        val START_TIME_MS = longPreferencesKey("start_time_ms")
        val TOTAL_WAIT_MS = longPreferencesKey("total_wait_ms")
        val MEAT_WAIT_MINUTES = intPreferencesKey("meat_wait_minutes")
        val DAIRY_WAIT_MINUTES = intPreferencesKey("dairy_wait_minutes")
    }

    val timerState: Flow<KashrutTimerState> = context.dataStore.data.map { prefs ->
        KashrutTimerState(
            state = runCatching {
                KashrutState.valueOf(prefs[Keys.STATE] ?: KashrutState.NEUTRAL.name)
            }.getOrDefault(KashrutState.NEUTRAL),
            startTimeMs = prefs[Keys.START_TIME_MS] ?: 0L,
            totalWaitMs = prefs[Keys.TOTAL_WAIT_MS] ?: 0L
        )
    }

    val waitSettings: Flow<WaitSettings> = context.dataStore.data.map { prefs ->
        WaitSettings(
            meatWaitMinutes = prefs[Keys.MEAT_WAIT_MINUTES] ?: 360,
            dairyWaitMinutes = prefs[Keys.DAIRY_WAIT_MINUTES] ?: 30
        )
    }

    suspend fun setTimerState(state: KashrutTimerState) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STATE] = state.state.name
            prefs[Keys.START_TIME_MS] = state.startTimeMs
            prefs[Keys.TOTAL_WAIT_MS] = state.totalWaitMs
        }
    }

    suspend fun setWaitSettings(settings: WaitSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MEAT_WAIT_MINUTES] = settings.meatWaitMinutes
            prefs[Keys.DAIRY_WAIT_MINUTES] = settings.dairyWaitMinutes
        }
    }

    suspend fun resetTimer() {
        context.dataStore.edit { prefs ->
            prefs[Keys.STATE] = KashrutState.NEUTRAL.name
            prefs[Keys.START_TIME_MS] = 0L
            prefs[Keys.TOTAL_WAIT_MS] = 0L
        }
    }
}
