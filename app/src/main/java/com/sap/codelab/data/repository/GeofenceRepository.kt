package com.sap.codelab.data.repository

import com.sap.codelab.data.model.GeofenceRegisterResult
import com.sap.codelab.data.model.Memo

interface GeofenceRepository {
    /**
     * Adds a new, or updates an existing geofence with the details of the specified [memo].
     *
     * @return The result of the operation.
     */
    suspend fun addGeofence(memo: Memo): GeofenceRegisterResult

    /**
     * Adds new, or updates existing geofences with the details of the specified [memos].
     *
     * When [triggerImmediately] is `true`, a geofence is triggered immediately
     * if the device is already inside that geofence.
     *
     * @return The result of the operation.
     */
    suspend fun addGeofences(
        memos: List<Memo>,
        triggerImmediately: Boolean = false,
    ): GeofenceRegisterResult

    /**
     * Removes the geofence for the specified [memo].
     */
    fun removeGeofence(memo: Memo)

    /**
     * Removes the geofence with the specified [memoId].
     */
    fun removeGeofence(memoId: Long)

    /**
     * Removes the geofences with the IDs listed in the specified [memoIds].
     */
    fun removeGeofences(memoIds: List<Long>)
}
