package com.sap.codelab.receiver

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.sap.codelab.MainActivity
import com.sap.codelab.R
import com.sap.codelab.SAPMemo
import com.sap.codelab.data.model.Memo
import com.sap.codelab.data.repository.GeofenceRepository
import com.sap.codelab.data.repository.MemoRepository
import com.sap.codelab.data.repository.impl.GeofenceRepositoryImpl
import com.sap.codelab.data.repository.impl.MemoRepositoryImpl
import com.sap.codelab.util.hasNotificationPermission
import com.sap.codelab.worker.GeofenceRegisterWorker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

/**
 * A broadcast receiver for triggered geofences.
 */
class GeofenceReceiver : BroadcastReceiver() {

    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val geofenceRepository: GeofenceRepository = GeofenceRepositoryImpl
    private val memoRepository: MemoRepository = MemoRepositoryImpl

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return

        if (event.hasError()) {
            if (event.errorCode == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {
                GeofenceRegisterWorker.schedule(context)
            }

            return
        }

        // We are also registered for Geofence.GEOFENCE_TRANSITION_EXIT, skip anything but ENTER
        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) {
            return
        }

        val geofences = event.triggeringGeofences ?: return
        val pendingResult = goAsync()

        coroutineScope.launch {
            try {
                withTimeoutOrNull(5.seconds) {
                    for (geofence in geofences) {
                        val memoId = geofence.requestId.toLongOrNull() ?: continue

                        try {
                            handleEvent(context.applicationContext, memoId)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to handle ENTER transition for memo id $memoId", e)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleEvent(context: Context, memoId: Long) {
        val memo = memoRepository.getMemoById(memoId)

        // The memo might have been removed or already closed, remove the geofence
        if (memo == null || memo.isDone) {
            geofenceRepository.removeGeofence(memoId)
            return
        }

        postNotification(context, memo)
    }

    private fun postNotification(context: Context, memo: Memo) {
        if (!context.hasNotificationPermission()) {
            Log.w(TAG, "Cannot show geofence event notification: Missing permission")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(SAPMemo.INTENT_EXTRA_MEMO_ID, memo.id)
        }
        val pendingIntent = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(intent)
            .getPendingIntent(
                memo.id.toInt(),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        val memoDescription = memo.description.take(MAX_DESCRIPTION_LENGTH)
        val notification = NotificationCompat.Builder(context, SAPMemo.NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_globe_location_pin)
            .setContentTitle(memo.title)
            .setContentText(memoDescription)
            .setStyle(NotificationCompat.BigTextStyle().bigText(memoDescription))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // The permission check is performed by Context.hasNotificationPermission above
        @SuppressLint("MissingPermission")
        NotificationManagerCompat.from(context).notify(memo.id.toInt(), notification)
    }
}

private const val MAX_DESCRIPTION_LENGTH = 140
private const val TAG = "GeofenceReceiver"
