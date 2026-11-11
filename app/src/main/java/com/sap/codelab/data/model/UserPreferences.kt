package com.sap.codelab.data.model

/**
 * Represents the user's preferences for displayed permission requests.
 */
data class UserPreferences(
    /**
     * The user allows fine location permission to be requested.
     */
    val allowsFineLocationRequests: Boolean = false,

    /**
     * The user allows background location permission to be requested.
     */
    val allowsBackgroundLocationRequests: Boolean = false,

    /**
     * The user allows notification permission to be requested.
     */
    val allowsNotificationRequests: Boolean = false,
)
