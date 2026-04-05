package com.yeudaby.sixheaven.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yeudaby.sixheaven.data.model.KashrutState
import com.yeudaby.sixheaven.ui.theme.*
import com.yeudaby.sixheaven.util.TimeUtils

@Composable
fun CircularProgressTimer(
    progress: Float,
    remainingMs: Long,
    state: KashrutState,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (isActive) progress else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "timerProgress"
    )

    val accentColor = when (state) {
        KashrutState.MEATY   -> Burgundy
        KashrutState.DAIRY   -> SkyBlue
        KashrutState.NEUTRAL -> SageGreen
    }
    val trackColor = accentColor.copy(alpha = 0.10f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // ── Ring ──────────────────────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 9.dp.toPx()
            val diameter = size.minDimension - strokeWidth * 2
            val topLeft = Offset(
                x = (size.width - diameter) / 2f,
                y = (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            // Background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Elapsed-time arc (fills clockwise as time passes)
            if (isActive && animatedProgress > 0f) {
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // ── Center content ────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isActive) {
                Text(
                    text = TimeUtils.formatMillisToHhMmSs(remainingMs),
                    // displayLarge = 54sp Light – thin, premium countdown
                    style = MaterialTheme.typography.displayLarge,
                    color = accentColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "נותר",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            } else {
                Text(
                    text = "✓",
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Light,
                    color = SageGreen
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "פרווה",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
