package com.sap.codelab.data.repository.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sap.codelab.data.model.UserPreferences
import com.sap.codelab.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object PreferencesRepositoryImpl : PreferencesRepository {

    private lateinit var appContext: Context

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_prefs")

    private val fineLocationPreference = booleanPreferencesKey("fine_location_allowed")
    private val backgroundLocationPreference = booleanPreferencesKey("background_location_allowed")
    private val notificationPreference = booleanPreferencesKey("notification_allowed")

    override fun getPreferences(): Flow<UserPreferences> = appContext.dataStore.data.map { prefs ->
        UserPreferences(
            allowsFineLocationRequests = prefs[fineLocationPreference] ?: true,
            allowsBackgroundLocationRequests = prefs[backgroundLocationPreference] ?: true,
            allowsNotificationRequests = prefs[notificationPreference] ?: true,
        )
    }

    override suspend fun updatePreferences(preferences: UserPreferences) {
        appContext.dataStore.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[fineLocationPreference] = preferences.allowsFineLocationRequests
                prefs[backgroundLocationPreference] = preferences.allowsBackgroundLocationRequests
                prefs[notificationPreference] = preferences.allowsNotificationRequests
            }
        }
    }

    fun initialize(applicationContext: Context) {
        if (!::appContext.isInitialized) {
            appContext = applicationContext
        }
    }
}
