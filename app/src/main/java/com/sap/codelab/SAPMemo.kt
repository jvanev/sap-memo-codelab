package com.sap.codelab

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class SAPMemo : Application() {
    override fun onCreate() {
        super.onCreate()

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH,
                ),
            )
    }

    companion object {
        const val NOTIFICATION_CHANNEL = "geofence_channel"
    }
}
