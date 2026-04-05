package com.yeudaby.sixheaven.ui.main

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yeudaby.sixheaven.R
import com.yeudaby.sixheaven.data.model.KashrutState
import com.yeudaby.sixheaven.ui.components.*
import com.yeudaby.sixheaven.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timerState = uiState.timerState
    var permCardDismissed by remember { mutableStateOf(false) }
    val showPermCard = !hasNotificationPermission && !permCardDismissed

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        // ── Animated background blobs (behind everything) ─────────────────────
        BackgroundBlobs(state = timerState.state)

        // ── Scaffold (transparent, so blobs show through) ─────────────────────
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.logo_full),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Burgundy
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings_title),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Notification permission rationale ─────────────────────────
                AnimatedVisibility(
                    visible = showPermCard,
                    enter = fadeIn() + expandVertically(),
                    exit  = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        PermissionRationaleCard(
                            onGrantClick = {
                                permCardDismissed = true
                                onRequestNotificationPermission()
                            },
                            onDismiss = { permCardDismissed = true }
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // Push timer to vertical center
                Spacer(Modifier.weight(1f))

                // ── State badge ───────────────────────────────────────────────
                AnimatedContent(
                    targetState = timerState.state,
                    transitionSpec = { fadeIn(tween(450)) togetherWith fadeOut(tween(300)) },
                    label = "stateBadge"
                ) { state ->
                    StateBadge(state = state)
                }

                Spacer(Modifier.height(20.dp))

                // ── Timer glass card ──────────────────────────────────────────
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 24.dp
                ) {
                    CircularProgressTimer(
                        progress   = timerState.progressFraction,
                        remainingMs = uiState.remainingMs,
                        state      = timerState.state,
                        isActive   = timerState.isActive,
                        modifier   = Modifier
                            .size(240.dp)
                            .align(Alignment.Center)
                    )
                }

                // Push buttons to bottom
                Spacer(Modifier.weight(1f))

                // ── Action buttons glass card ─────────────────────────────────
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 16.dp
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MealButton(
                                text = stringResource(R.string.btn_ate_meat),
                                onClick = { viewModel.ateMeat() },
                                enabled = !timerState.isActive,
                                containerColor = Burgundy,
                                modifier = Modifier.weight(1f)
                            )
                            MealButton(
                                text = stringResource(R.string.btn_ate_dairy),
                                onClick = { viewModel.ateDairy() },
                                enabled = !timerState.isActive,
                                containerColor = SkyBlue,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        AnimatedVisibility(visible = timerState.isActive) {
                            TextButton(
                                onClick = { viewModel.cancelTimer() },
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = stringResource(R.string.btn_cancel),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

// ── State badge pill ──────────────────────────────────────────────────────────

@Composable
private fun StateBadge(state: KashrutState) {
    val (bgColor, textColor, label) = when (state) {
        KashrutState.NEUTRAL -> Triple(
            SageGreenLight, SageGreen,
            "אתה פרווה ✓"
        )
        KashrutState.MEATY -> Triple(
            BurgundyLight, Burgundy,
            "ממתינים לחלבי 🥩"
        )
        KashrutState.DAIRY -> Triple(
            SkyBlueLight, SkyBlue,
            "ממתינים לבשרי 🥛"
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        shadowElevation = 2.dp
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}
