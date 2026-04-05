package com.yeudaby.sixheaven.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yeudaby.sixheaven.R
import com.yeudaby.sixheaven.ui.theme.Burgundy
import com.yeudaby.sixheaven.ui.theme.TextSecondary

/**
 * A dialog that lets the user pick a custom wait duration (hours + minutes).
 * Shown when the user selects "Custom" in settings.
 */
@Composable
fun CustomTimePickerDialog(
    initialMinutes: Int,
    onConfirm: (totalMinutes: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var hours by remember { mutableIntStateOf(initialMinutes / 60) }
    var minutes by remember { mutableIntStateOf(initialMinutes % 60) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = stringResource(R.string.custom_time_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberColumn(
                    value = hours,
                    label = stringResource(R.string.custom_time_hours),
                    range = 0..23,
                    onValueChange = { hours = it }
                )
                Text(
                    text = ":",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Light,
                    color = TextSecondary,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .align(Alignment.CenterVertically)
                )
                NumberColumn(
                    value = minutes,
                    label = stringResource(R.string.custom_time_minutes),
                    range = 0..59,
                    onValueChange = { minutes = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(hours * 60 + minutes) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Burgundy)
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_word))
            }
        }
    )
}

@Composable
private fun NumberColumn(
    value: Int,
    label: String,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        IconButton(
            onClick = { if (value < range.last) onValueChange(value + 1) },
            enabled = value < range.last
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "הגדל")
        }
        Text(
            text = "%02d".format(value),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(80.dp)
        )
        IconButton(
            onClick = { if (value > range.first) onValueChange(value - 1) },
            enabled = value > range.first
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "הפחת")
        }
    }
}
