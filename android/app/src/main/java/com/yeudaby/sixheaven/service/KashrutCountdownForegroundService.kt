package com.yeudaby.sixheaven.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.glance.appwidget.updateAll
import com.yeudaby.sixheaven.MainActivity
import com.yeudaby.sixheaven.R
import com.yeudaby.sixheaven.SixHeavenApplication.Companion.CHANNEL_COUNTDOWN
import com.yeudaby.sixheaven.SixHeavenApplication.Companion.NOTIFICATION_ID_COUNTDOWN
import com.yeudaby.sixheaven.data.model.KashrutState
import com.yeudaby.sixheaven.data.repository.KashrutRepository
import com.yeudaby.sixheaven.util.TimeUtils
import com.yeudaby.sixheaven.widget.SixHeavenGlanceWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

// ── Brand ARGB ints (mirrors Color.kt, usable outside Compose) ─────────────
private val COLOR_BURGUNDY   = Color.parseColor("#800A2C")
private val COLOR_SKY_BLUE   = Color.parseColor("#38BDF8")
private val COLOR_SAGE_GREEN = Color.parseColor("#059669")

@AndroidEntryPoint
class KashrutCountdownForegroundService : Service() {

    @Inject lateinit var repository: KashrutRepository

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickJob: Job? = null
    private var widgetTickCount = 0

    // Cached once to avoid re-drawing the vector drawable on every tick
    private val logoBitmap: Bitmap by lazy { buildLogoBitmap() }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startCountdown()
            ACTION_STOP  -> stopSelf()
        }
        return START_STICKY
    }

    private fun startCountdown() {
        val placeholder = buildNotification(
            title = getString(R.string.app_name),
            text  = getString(R.string.notification_countdown_default),
            color = COLOR_BURGUNDY
        )

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID_COUNTDOWN,
            placeholder,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )

        startTicker()
    }

    private fun startTicker() {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (isActive) {
                updateNotification()
                widgetTickCount++
                if (widgetTickCount % 60 == 0) updateWidget()
                delay(1_000L)
            }
        }
    }

    private suspend fun updateNotification() {
        val timerState = repository.timerState.first()
        if (!timerState.isActive) { stopSelf(); return }

        val (title, accentColor) = when (timerState.state) {
            KashrutState.MEATY   -> getString(R.string.notif_countdown_title_meat) to COLOR_BURGUNDY
            KashrutState.DAIRY   -> getString(R.string.notif_countdown_title_dairy) to COLOR_SKY_BLUE
            KashrutState.NEUTRAL -> { stopSelf(); return }
        }

        val timeText = getString(
            R.string.notif_remaining_format,
            TimeUtils.formatMillisToHhMmSs(timerState.remainingMs)
        )

        getSystemService(NotificationManager::class.java).notify(
            NOTIFICATION_ID_COUNTDOWN,
            buildNotification(title = title, text = timeText, color = accentColor)
        )
    }

    private fun buildNotification(title: String, text: String, color: Int) =
        NotificationCompat.Builder(this, CHANNEL_COUNTDOWN)
            .setSmallIcon(R.drawable.black_and_white_logo)
//            .setLargeIcon(logoBitmap)
            .setContentTitle(title)
            .setContentText(text)
            .setColor(color)
            .setColorized(false)
            .setContentIntent(launchPendingIntent())
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .build()

    /** Rasterises logo.xml at 192 px so it can be used as a large notification icon. */
    private fun buildLogoBitmap(): Bitmap {
        val px = (48 * resources.displayMetrics.density).toInt()
        val drawable = checkNotNull(getDrawable(R.drawable.logo_full))
        val bmp = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, px, px)
        drawable.draw(Canvas(bmp))
        return bmp
    }

    private fun launchPendingIntent() = PendingIntent.getActivity(
        this, 0,
        Intent(this, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE
    )

    private fun updateWidget() {
        scope.launch {
            SixHeavenGlanceWidget().updateAll(this@KashrutCountdownForegroundService)
        }
    }

    override fun onDestroy() {
        tickJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "ACTION_START_COUNTDOWN"
        const val ACTION_STOP  = "ACTION_STOP_COUNTDOWN"
    }
}
