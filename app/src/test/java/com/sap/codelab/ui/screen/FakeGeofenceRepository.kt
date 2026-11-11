package com.sap.codelab.ui.screen

import com.sap.codelab.data.model.GeofenceRegisterResult
import com.sap.codelab.data.model.Memo
import com.sap.codelab.data.repository.GeofenceRepository

/**
 * Records geofence calls instead of talking to Play services.
 */
class FakeGeofenceRepository : GeofenceRepository {

    /** The result every add call returns. */
    var result: GeofenceRegisterResult = GeofenceRegisterResult.Success

    /** Every memo passed to an add call, in order. */
    val registeredMemos = mutableListOf<Memo>()

    /** Every memo ID passed to a remove call, in order. */
    val removedIds = mutableListOf<Long>()

    override suspend fun addGeofence(memo: Memo): GeofenceRegisterResult =
        addGeofences(listOf(memo))

    override suspend fun addGeofences(
        memos: List<Memo>,
        triggerImmediately: Boolean,
    ): GeofenceRegisterResult {
        registeredMemos += memos
        return result
    }

    override fun removeGeofence(memo: Memo) {
        removedIds += memo.id
    }

    override fun removeGeofence(memoId: Long) {
        removedIds += memoId
    }

    override fun removeGeofences(memoIds: List<Long>) {
        removedIds += memoIds
    }
}
