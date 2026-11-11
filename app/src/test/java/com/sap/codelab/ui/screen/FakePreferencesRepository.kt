package com.sap.codelab.ui.screen

import com.sap.codelab.data.model.UserPreferences
import com.sap.codelab.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory [PreferencesRepository]. Starts like a fresh install: every request allowed,
 * matching the defaults PreferencesRepositoryImpl uses for missing keys.
 */
class FakePreferencesRepository(
    initial: UserPreferences = UserPreferences(
        allowsFineLocationRequests = true,
        allowsBackgroundLocationRequests = true,
        allowsNotificationRequests = true,
    ),
) : PreferencesRepository {

    private val preferences = MutableStateFlow(initial)

    val stored: UserPreferences get() = preferences.value

    override fun getPreferences(): Flow<UserPreferences> = preferences

    override suspend fun denyFineLocationRequests() =
        preferences.update { it.copy(allowsFineLocationRequests = false) }

    override suspend fun denyBackgroundLocationRequests() =
        preferences.update { it.copy(allowsBackgroundLocationRequests = false) }

    override suspend fun denyNotificationRequests() =
        preferences.update { it.copy(allowsNotificationRequests = false) }
}
