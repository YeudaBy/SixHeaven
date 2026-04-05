package com.yeudaby.sixheaven.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yeudaby.sixheaven.BuildConfig
import com.yeudaby.sixheaven.R
import com.yeudaby.sixheaven.ui.components.GlassCard
import com.yeudaby.sixheaven.ui.theme.*

@Composable
fun AboutCard(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 6.dp,
        contentPadding = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Header: logo mark + app identity ─────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Burgundy.copy(alpha = 0.08f),
                                SkyBlue.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // App logo
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "SixHeaven logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(80.dp)
                    )

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "SixHeaven",
                            style = MaterialTheme.typography.titleLarge,
                            color = Burgundy,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "v${BuildConfig.VERSION_NAME} · כשרות בכיס",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            HorizontalDivider(color = Burgundy.copy(alpha = 0.08f))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Creator ───────────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "נוצר באהבה ❤️ על ידי",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "Yeuda By",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                // ── Links ─────────────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinkRow(
                        icon       = Icons.Default.Language,
                        label      = "yeudaby.com",
                        sublabel   = "האתר האישי",
                        accentColor = Burgundy,
                        onClick    = { uriHandler.openUri("https://yeudaby.com") }
                    )
                    LinkRow(
                        icon       = Icons.Default.Smartphone,
                        label      = "sixheaven.yeudaby.com",
                        sublabel   = "דף הנחיתה של האפליקציה",
                        accentColor = SkyBlue,
                        onClick    = { uriHandler.openUri("https://sixheaven.yeudaby.com") }
                    )
                    LinkRow(
                        icon       = Icons.Default.Code,
                        label      = "YeudaBy / SixHeaven",
                        sublabel   = "קוד פתוח ב-GitHub",
                        accentColor = Color(0xFF1F2937),
                        onClick    = { uriHandler.openUri("https://github.com/YeudaBy/SixHeaven/") }
                    )
                }
            }

            // ── Footer ────────────────────────────────────────────────────────
            HorizontalDivider(color = Burgundy.copy(alpha = 0.06f))
            Text(
                text = "© 2025 Yeuda By · כל הזכויות שמורות",
                style = MaterialTheme.typography.labelMedium,
                color = TextDisabled,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }
    }
}

// ── Link row: icon  •  label + sublabel  •  open icon ────────────────────────

@Composable
private fun LinkRow(
    icon: ImageVector,
    label: String,
    sublabel: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(accentColor.copy(alpha = 0.07f))
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = accentColor.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        // Tinted icon container
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .background(accentColor.copy(alpha = 0.12f), CircleShape)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
            Text(
                text = sublabel,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }

        Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = "פתח",
            tint = accentColor.copy(alpha = 0.45f),
            modifier = Modifier.size(15.dp)
        )
    }
}
