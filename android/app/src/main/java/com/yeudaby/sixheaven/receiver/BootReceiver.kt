package com.yeudaby.sixheaven.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yeudaby.sixheaven.data.repository.KashrutRepository
import com.yeudaby.sixheaven.service.KashrutCountdownForegroundService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Restarts the countdown foreground service (and re-arms the alarm) after a device reboot,
 * provided a kashrut timer was active when the device powered off.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: KashrutRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        val async = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val timerState = repository.timerState.first()
                if (!timerState.isActive) return@launch

                val remaining = timerState.remainingMs
                if (remaining <= 0L) {
                    // Timer expired while device was off – just reset state
                    repository.cancelTimer()
                    return@launch
                }

                // Restart the foreground service
                context.startForegroundService(
                    Intent(context, KashrutCountdownForegroundService::class.java)
                        .setAction(KashrutCountdownForegroundService.ACTION_START)
                )

                // Re-arm the exact alarm
                val alarmManager = context.getSystemService(AlarmManager::class.java)
                val alarmIntent = PendingIntent.getBroadcast(
                    context, 2001,
                    Intent(context, AlarmReceiver::class.java).apply {
                        putExtra(AlarmReceiver.EXTRA_STATE, timerState.state.name)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timerState.endTimeMs,
                    alarmIntent
                )
            } finally {
                async.finish()
            }
        }
    }
}
