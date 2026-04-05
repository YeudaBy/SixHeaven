package com.yeudaby.sixheaven

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SixHeavenApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        // Low-priority channel for the persistent countdown foreground notification
        val countdownChannel = NotificationChannel(
            CHANNEL_COUNTDOWN,
            "Kashrut Countdown",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows the active kashrut waiting countdown"
            setShowBadge(false)
        }

        // High-priority channel for the "timer done!" alert
        val alertChannel = NotificationChannel(
            CHANNEL_ALERT,
            "Kashrut Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when a waiting period ends"
        }

        manager.createNotificationChannels(listOf(countdownChannel, alertChannel))
    }

    companion object {
        const val CHANNEL_COUNTDOWN = "channel_countdown"
        const val CHANNEL_ALERT     = "channel_alert"
        const val NOTIFICATION_ID_COUNTDOWN = 1001
        const val NOTIFICATION_ID_ALERT     = 1002
    }
}
