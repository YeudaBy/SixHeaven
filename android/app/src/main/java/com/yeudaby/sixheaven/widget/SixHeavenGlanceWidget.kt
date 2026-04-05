package com.yeudaby.sixheaven.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.material3.ColorProviders
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.yeudaby.sixheaven.MainActivity
import com.yeudaby.sixheaven.data.datastore.KashrutDataStore
import com.yeudaby.sixheaven.data.model.KashrutState
import com.yeudaby.sixheaven.util.TimeUtils
import kotlinx.coroutines.flow.first

class SixHeavenGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Read current state directly from DataStore (Glance can't use Hilt DI easily)
        val dataStore = KashrutDataStore(context)
        val timerState = dataStore.timerState.first()

        provideContent {
            WidgetContent(
                state = timerState.state,
                remainingMs = timerState.remainingMs,
                isActive = timerState.isActive
            )
        }
    }

    @Composable
    private fun WidgetContent(
        state: KashrutState,
        remainingMs: Long,
        isActive: Boolean
    ) {
        val bgColor = when (state) {
            KashrutState.MEATY   -> ColorProvider(Color(0xFFFFDDD8))
            KashrutState.DAIRY   -> ColorProvider(Color(0xFFD8EFFF))
            KashrutState.NEUTRAL -> ColorProvider(Color(0xFFDEF5E4))
        }
        val textColor = when (state) {
            KashrutState.MEATY   -> ColorProvider(Color(0xFFC0392B))
            KashrutState.DAIRY   -> ColorProvider(Color(0xFF1A8FBF))
            KashrutState.NEUTRAL -> ColorProvider(Color(0xFF27AE60))
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bgColor)
                .clickable(actionStartActivity<MainActivity>())
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (state) {
                        KashrutState.NEUTRAL -> "פרווה ✓"
                        KashrutState.MEATY   -> "🥩 ממתינים לחלבי"
                        KashrutState.DAIRY   -> "🥛 ממתינים לבשרי"
                    },
                    style = TextStyle(
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
                if (isActive) {
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = TimeUtils.formatMillisToHhMmSs(remainingMs),
                        style = TextStyle(
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }
            }
        }
    }
}

class SixHeavenGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SixHeavenGlanceWidget()
}
