package com.yeudaby.sixheaven.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.yeudaby.sixheaven.MainActivity
import com.yeudaby.sixheaven.R
import com.yeudaby.sixheaven.SixHeavenApplication.Companion.CHANNEL_ALERT
import com.yeudaby.sixheaven.SixHeavenApplication.Companion.NOTIFICATION_ID_ALERT
import com.yeudaby.sixheaven.data.model.KashrutState
import com.yeudaby.sixheaven.data.repository.KashrutRepository
import com.yeudaby.sixheaven.service.KashrutCountdownForegroundService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

// ── Brand ARGB ints ──────────────────────────────────────────────────────────
private val COLOR_BURGUNDY  = Color.parseColor("#800A2C")
private val COLOR_SKY_BLUE  = Color.parseColor("#38BDF8")

/**
 * Fired by AlarmManager at the exact moment a kashrut waiting period ends.
 * Shows a high-priority, brand-colored notification and resets timer state.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: KashrutRepository

    override fun onReceive(context: Context, intent: Intent) {
        val stateName = intent.getStringExtra(EXTRA_STATE) ?: return
        val state = runCatching { KashrutState.valueOf(stateName) }.getOrNull() ?: return

        // Stop the foreground countdown service
        context.startService(
            Intent(context, KashrutCountdownForegroundService::class.java)
                .setAction(KashrutCountdownForegroundService.ACTION_STOP)
        )

        showCompletionNotification(context, state)

        val async = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try { repository.cancelTimer() } finally { async.finish() }
        }
    }

    private fun showCompletionNotification(context: Context, state: KashrutState) {
        // When MEATY timer ends → transition to dairy → SkyBlue accent
        // When DAIRY timer ends → transition to meat  → Burgundy accent
        val (message, accentColor) = when (state) {
            KashrutState.MEATY   ->
                context.getString(R.string.notif_end_meat)  to COLOR_SKY_BLUE
            KashrutState.DAIRY   ->
                context.getString(R.string.notif_end_dairy) to COLOR_BURGUNDY
            KashrutState.NEUTRAL -> return
        }

        val tapIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERT)
            .setSmallIcon(R.drawable.logo_full)
            .setLargeIcon(context.logoBitmap())
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setColor(accentColor)           // brand accent stripe
            .setColorized(false)
            .setContentIntent(tapIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID_ALERT, notification)
    }

    companion object {
        const val EXTRA_STATE = "extra_kashrut_state"
    }
}

/** Rasterises logo.xml at 48dp so it can be used as a large notification icon. */
private fun Context.logoBitmap(): Bitmap {
    val px = (48 * resources.displayMetrics.density).toInt()
    val drawable = checkNotNull(getDrawable(R.drawable.logo_full))
    val bmp = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
    drawable.setBounds(0, 0, px, px)
    drawable.draw(Canvas(bmp))
    return bmp
}
