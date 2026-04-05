package com.yeudaby.sixheaven.tile

import android.app.PendingIntent
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.yeudaby.sixheaven.MainActivity
import com.yeudaby.sixheaven.data.datastore.KashrutDataStore
import com.yeudaby.sixheaven.data.model.KashrutState
import com.yeudaby.sixheaven.data.model.KashrutTimerState
import com.yeudaby.sixheaven.util.TimeUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

// ── Brand ARGB ints ──────────────────────────────────────────────────────────
private val COLOR_BURGUNDY   = Color.parseColor("#800A2C")
private val COLOR_SKY_BLUE   = Color.parseColor("#38BDF8")
private val COLOR_SAGE_GREEN = Color.parseColor("#059669")

/**
 * Quick Settings tile showing the current kashrut state.
 *
 * On API 31+ the tile icon is tinted with the brand color for that state
 * (Burgundy for meat, Sky Blue for dairy, Sage Green when pareve).
 * Tapping opens the main screen.
 */
class SixHeavenTileService : TileService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pi = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pi)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    private fun refreshTile() {
        scope.launch {
            val timerState = KashrutDataStore(this@SixHeavenTileService).timerState.first()
            withContext(Dispatchers.Main) {
                qsTile?.apply {
                    applyBrandColor(timerState.state)
                    when (timerState.state) {
                        KashrutState.NEUTRAL -> {
                            state = Tile.STATE_INACTIVE
                            label = "SixHeaven"
                            setSubtitleCompat("פרווה ✓")
                        }
                        KashrutState.MEATY -> {
                            state = Tile.STATE_ACTIVE
                            label = "🥩 בשרי"
                            setSubtitleCompat(TimeUtils.formatMillisToHhMmSs(timerState.remainingMs))
                        }
                        KashrutState.DAIRY -> {
                            state = Tile.STATE_ACTIVE
                            label = "🥛 חלבי"
                            setSubtitleCompat(TimeUtils.formatMillisToHhMmSs(timerState.remainingMs))
                        }
                    }
                    updateTile()
                }
            }
        }
    }

    /**
     * Tints the tile icon with the brand color for the current state.
     * API 31+ only — older devices keep the system default tint.
     */
    @Suppress("NewApi")
    private fun Tile.applyBrandColor(state: KashrutState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val color = when (state) {
                KashrutState.MEATY   -> COLOR_BURGUNDY
                KashrutState.DAIRY   -> COLOR_SKY_BLUE
                KashrutState.NEUTRAL -> COLOR_SAGE_GREEN
            }
//            iconTint = ColorStateList.valueOf(color)
        }
    }

    /** Sets the subtitle safely across API levels. */
    private fun Tile.setSubtitleCompat(text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            subtitle = text
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        scope.coroutineContext.cancelChildren()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
