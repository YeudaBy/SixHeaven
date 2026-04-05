package com.yeudaby.sixheaven.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ButtonShape = RoundedCornerShape(18.dp)

/**
 * Premium gradient button with colored shadow – no standard Material Button styling.
 */
@Composable
fun MealButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Slightly darker shade for the gradient end
    val darkColor = Color(
        red   = (containerColor.red   * 0.78f).coerceIn(0f, 1f),
        green = (containerColor.green * 0.78f).coerceIn(0f, 1f),
        blue  = (containerColor.blue  * 0.78f).coerceIn(0f, 1f)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(64.dp)
            .alpha(if (enabled) 1f else 0.38f)
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = ButtonShape,
                ambientColor = containerColor.copy(alpha = 0.35f),
                spotColor   = containerColor.copy(alpha = 0.45f)
            )
            .clip(ButtonShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(containerColor, darkColor)
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.20f),
                shape = ButtonShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.White.copy(alpha = 0.3f)),
                enabled = enabled,
                onClick = onClick
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )
    }
}
