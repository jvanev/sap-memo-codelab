package com.sap.codelab.data.repository.impl

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.PendingIntentCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.sap.codelab.data.model.GeofenceRegisterResult
import com.sap.codelab.data.model.Memo
import com.sap.codelab.data.repository.GeofenceRepository
import com.sap.codelab.receiver.GeofenceReceiver
import com.sap.codelab.util.hasGeofencingPermissions
import com.sap.codelab.util.isPlayServicesAvailable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object GeofenceRepositoryImpl : GeofenceRepository {

    /*
     * Observed behavior: Registering only for ENTER transitions causes
     * the geofences to be triggered only once.
     *
     * Workaround: Register for both ENTER and EXIT transitions, and ignore EXIT in the receiver.
     */
    private const val TRANSITION_TYPES = Geofence.GEOFENCE_TRANSITION_ENTER or
            Geofence.GEOFENCE_TRANSITION_EXIT

    private const val TRIGGER_RADIUS = 200f
    private const val GEOFENCE_RESPONSIVENESS = 5000

    private lateinit var appContext: Context
    private lateinit var geofencingClient: GeofencingClient

    override suspend fun addGeofence(memo: Memo): GeofenceRegisterResult {
        return addGeofences(listOf(memo))
    }

    override suspend fun addGeofences(
        memos: List<Memo>,
        triggerImmediately: Boolean,
    ): GeofenceRegisterResult = when {
        memos.isEmpty() -> GeofenceRegisterResult.Success

        !appContext.hasGeofencingPermissions() -> GeofenceRegisterResult.MissingPermission

        !appContext.isPlayServicesAvailable() -> GeofenceRegisterResult.PlayServicesUnavailable

        else -> registerGeofence(memos, triggerImmediately)
    }

    private suspend fun registerGeofence(
        memos: List<Memo>,
        triggerImmediately: Boolean,
    ) = suspendCancellableCoroutine { continuation ->
        val geofences = memos.map { memo ->
            Geofence.Builder()
                .setRequestId(memo.id.toString())
                .setCircularRegion(memo.reminderLatitude, memo.reminderLongitude, TRIGGER_RADIUS)
                .setNotificationResponsiveness(GEOFENCE_RESPONSIVENESS)
                .setTransitionTypes(TRANSITION_TYPES)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build()
        }
        val request = GeofencingRequest.Builder().apply {
            addGeofences(geofences)

            if (triggerImmediately) {
                setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            }
        }.build()

        try {
            geofencingClient.addGeofences(request, createPendingIntent()).also { task ->
                task.addOnSuccessListener {
                    continuation.resume(GeofenceRegisterResult.Success)
                }
                task.addOnFailureListener {
                    continuation.resume(it.toRegisterResult())
                }
            }
        } catch (_: SecurityException) {
            continuation.resume(GeofenceRegisterResult.MissingPermission)
        }
    }

    private fun Exception.toRegisterResult(): GeofenceRegisterResult = when {
        this is SecurityException -> GeofenceRegisterResult.MissingPermission

        this !is ApiException -> GeofenceRegisterResult.Failure(this)

        statusCode == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE ->
            GeofenceRegisterResult.LocationUnavailable

        statusCode == GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES ->
            GeofenceRegisterResult.LimitReached

        statusCode == GeofenceStatusCodes.GEOFENCE_INSUFFICIENT_LOCATION_PERMISSION ->
            GeofenceRegisterResult.MissingPermission

        else -> GeofenceRegisterResult.Failure(this)
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(appContext, GeofenceReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntentCompat.getBroadcast(appContext, 0, intent, flags, true)

        // May return null only if PendingIntent.FLAG_NO_CREATE has been supplied
        return requireNotNull(pendingIntent)
    }

    override fun removeGeofence(memo: Memo) {
        removeGeofences(listOf(memo.id))
    }

    override fun removeGeofence(memoId: Long) {
        removeGeofences(listOf(memoId))
    }

    override fun removeGeofences(memoIds: List<Long>) {
        geofencingClient.removeGeofences(memoIds.map { it.toString() }).addOnFailureListener {
            Log.w(TAG, "Failed to remove the list of geofences", it)
        }
    }

    fun initialize(applicationContext: Context) {
        if (!::geofencingClient.isInitialized) {
            appContext = applicationContext.applicationContext
            geofencingClient = LocationServices.getGeofencingClient(applicationContext)
        }
    }

    private const val TAG = "MemoGeofence"
}
