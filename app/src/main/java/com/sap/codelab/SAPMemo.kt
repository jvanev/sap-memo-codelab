package com.sap.codelab

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.sap.codelab.data.repository.impl.GeofenceRepositoryImpl
import com.sap.codelab.data.repository.impl.MemoRepositoryImpl
import com.sap.codelab.data.repository.impl.PreferencesRepositoryImpl

class SAPMemo : Application() {
    override fun onCreate() {
        super.onCreate()
        GeofenceRepositoryImpl.initialize(this)
        MemoRepositoryImpl.initialize(this)
        PreferencesRepositoryImpl.initialize(this)

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
        const val INTENT_EXTRA_MEMO_ID = "sap_memo_id"
    }
}
