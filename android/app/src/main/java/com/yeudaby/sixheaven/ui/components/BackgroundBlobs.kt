package com.yeudaby.sixheaven.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yeudaby.sixheaven.data.model.KashrutState
import com.yeudaby.sixheaven.ui.theme.*

/**
 * Two large blurred color blobs that animate between states.
 * On API 31+ they use a real BlurEffect for a frosted look; older devices get
 * a low-opacity fallback that still looks great.
 */
@Composable
fun BackgroundBlobs(
    state: KashrutState,
    modifier: Modifier = Modifier
) {
    val colorA by animateColorAsState(
        targetValue = when (state) {
            KashrutState.MEATY   -> BlobWarmA
            KashrutState.DAIRY   -> BlobCoolA
            KashrutState.NEUTRAL -> BlobFreshA
        },
        animationSpec = tween(900),
        label = "blobColorA"
    )
    val colorB by animateColorAsState(
        targetValue = when (state) {
            KashrutState.MEATY   -> BlobWarmB
            KashrutState.DAIRY   -> BlobCoolB
            KashrutState.NEUTRAL -> BlobFreshB
        },
        animationSpec = tween(900),
        label = "blobColorB"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // ── Top-right blob ────────────────────────────────────────────────────
        Blob(
            color = colorA,
            size = 380.dp,
            blurRadius = 130f,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-100).dp)
        )
        // ── Bottom-left blob ──────────────────────────────────────────────────
        Blob(
            color = colorB,
            size = 300.dp,
            blurRadius = 110f,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-70).dp, y = 70.dp)
        )
    }
}

@Suppress("NewApi")
@Composable
private fun Blob(
    color: androidx.compose.ui.graphics.Color,
    size: Dp,
    blurRadius: Float,
    modifier: Modifier = Modifier
) {
    val blurMod = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.graphicsLayer {
            renderEffect = BlurEffect(blurRadius, blurRadius, TileMode.Decal)
        }
    } else {
        // Fallback: larger soft circle without blur
        Modifier
    }

    Box(
        modifier = modifier
            .size(size)
            .then(blurMod)
            .background(
                color = color.copy(alpha = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 0.85f else 0.45f),
                shape = CircleShape
            )
    )
}
