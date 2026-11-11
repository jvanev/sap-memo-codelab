package com.sap.codelab.data.repository

import com.sap.codelab.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Implementations of this interface provide preferences-related CRUD operations.
 */
interface PreferencesRepository {
    /**
     * Returns a [Flow] of the user's preferences.
     */
    fun getPreferences(): Flow<UserPreferences>

    /**
     * Disables [UserPreferences.allowsFineLocationRequests].
     */
    suspend fun denyFineLocationRequests()

    /**
     * Disables [UserPreferences.allowsBackgroundLocationRequests]
     */
    suspend fun denyBackgroundLocationRequests()

    /**
     * Disables [UserPreferences.allowsNotificationRequests]
     */
    suspend fun denyNotificationRequests()
}
