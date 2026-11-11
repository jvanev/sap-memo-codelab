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
     * Updates the stored user preferences with the values specified in [preferences].
     */
    suspend fun updatePreferences(preferences: UserPreferences)
}
