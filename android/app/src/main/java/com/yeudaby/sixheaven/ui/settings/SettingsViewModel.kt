package com.yeudaby.sixheaven.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yeudaby.sixheaven.data.model.WaitSettings
import com.yeudaby.sixheaven.data.repository.KashrutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: KashrutRepository
) : ViewModel() {

    val settings: StateFlow<WaitSettings> = repository.waitSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WaitSettings())

    fun updateMeatWait(minutes: Int) = viewModelScope.launch {
        repository.updateSettings(settings.value.copy(meatWaitMinutes = minutes))
    }

    fun updateDairyWait(minutes: Int) = viewModelScope.launch {
        repository.updateSettings(settings.value.copy(dairyWaitMinutes = minutes))
    }
}
