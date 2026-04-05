package com.yeudaby.sixheaven.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yeudaby.sixheaven.R
import com.yeudaby.sixheaven.ui.components.CustomTimePickerDialog
import com.yeudaby.sixheaven.ui.components.GlassCard
import com.yeudaby.sixheaven.ui.theme.*

// ── Preset definitions ────────────────────────────────────────────────────────

private data class WaitOption(val minutes: Int, val label: String)

private val meatPresets = listOf(
    WaitOption(360, "6 שעות (ברירת מחדל)"),
    WaitOption(331, "רוב שש (5:31)"),    // ← fixed: majority of 6 = 5h 31m
    WaitOption(300, "5 שעות"),
    WaitOption(180, "3 שעות")
)
private val meatPresetMinutes = meatPresets.map { it.minutes }.toSet()

private val dairyPresets = listOf(
    WaitOption(30, "30 דקות (ברירת מחדל)"),
    WaitOption(60, "שעה אחת"),
    WaitOption(360, "6 שעות")
)
private val dairyPresetMinutes = dairyPresets.map { it.minutes }.toSet()

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var showMeatCustomPicker  by remember { mutableStateOf(false) }
    var showDairyCustomPicker by remember { mutableStateOf(false) }

    val isMeatCustom  = settings.meatWaitMinutes  !in meatPresetMinutes
    val isDairyCustom = settings.dairyWaitMinutes !in dairyPresetMinutes

    // ── Custom time picker dialogs ────────────────────────────────────────────
    if (showMeatCustomPicker) {
        CustomTimePickerDialog(
            initialMinutes = settings.meatWaitMinutes,
            onConfirm = { total ->
                viewModel.updateMeatWait(total)
                showMeatCustomPicker = false
            },
            onDismiss = { showMeatCustomPicker = false }
        )
    }
    if (showDairyCustomPicker) {
        CustomTimePickerDialog(
            initialMinutes = settings.dairyWaitMinutes,
            onConfirm = { total ->
                viewModel.updateDairyWait(total)
                showDairyCustomPicker = false
            },
            onDismiss = { showDairyCustomPicker = false }
        )
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.settings_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "חזרה",
                                tint = Burgundy
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // ── Meat wait setting ─────────────────────────────────────────
                SettingsGroup(title = stringResource(R.string.setting_wait_meat)) {
                    meatPresets.forEach { opt ->
                        OptionRow(
                            label = opt.label,
                            selected = settings.meatWaitMinutes == opt.minutes,
                            onSelect = { viewModel.updateMeatWait(opt.minutes) }
                        )
                    }
                    // Custom row
                    OptionRow(
                        label = if (isMeatCustom) {
                            val h = settings.meatWaitMinutes / 60
                            val m = settings.meatWaitMinutes % 60
                            stringResource(R.string.custom_time_current, h, m)
                        } else stringResource(R.string.custom_time_label),
                        selected = isMeatCustom,
                        onSelect = { showMeatCustomPicker = true }
                    )
                }

                // ── Dairy wait setting ────────────────────────────────────────
                SettingsGroup(title = stringResource(R.string.setting_wait_dairy)) {
                    dairyPresets.forEach { opt ->
                        OptionRow(
                            label = opt.label,
                            selected = settings.dairyWaitMinutes == opt.minutes,
                            onSelect = { viewModel.updateDairyWait(opt.minutes) }
                        )
                    }
                    OptionRow(
                        label = if (isDairyCustom) {
                            val h = settings.dairyWaitMinutes / 60
                            val m = settings.dairyWaitMinutes % 60
                            stringResource(R.string.custom_time_current, h, m)
                        } else stringResource(R.string.custom_time_label),
                        selected = isDairyCustom,
                        onSelect = { showDairyCustomPicker = true }
                    )
                }

                // ── About / Credits ───────────────────────────────────────────
                AboutCard(modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── Reusable composables ──────────────────────────────────────────────────────

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 6.dp,
        contentPadding = 16.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Burgundy,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun OptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (selected) Modifier.background(Burgundy.copy(alpha = 0.07f)) else Modifier
            )
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = Burgundy
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) Burgundy else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}
