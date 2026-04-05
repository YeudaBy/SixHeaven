package com.yeudaby.sixheaven.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background

private val GlassShape = RoundedCornerShape(24.dp)

/**
 * A semi-transparent white "glassmorphism light" card:
 * soft shadow → clip → 70% white fill → subtle white border.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 10.dp,
    contentPadding: Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = GlassShape,
                ambientColor = Color(0x12000000),
                spotColor   = Color(0x18000000)
            )
            .clip(GlassShape)
            .background(Color(0xB8FFFFFF))          // 72% white
            .border(1.dp, Color(0xCCFFFFFF), GlassShape)
            .padding(contentPadding),
        content = content
    )
}
