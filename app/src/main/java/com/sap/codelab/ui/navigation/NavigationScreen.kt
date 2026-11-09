package com.sap.codelab.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Represents a UI navigation destination.
 */
sealed interface NavigationScreen : NavKey {
    /**
     * Represents the main screen.
     * This is the first screen a user sees when the app starts.
     */
    @Serializable
    data object Home : NavigationScreen

    /**
     * Represents the screen for creating a new, or displaying the details of an existing [Memo].
     *
     * If no [memoId] is specified, the user will be able to create a new memo.
     */
    @Serializable
    data class Memo(val memoId: Long = 0) : NavigationScreen
}
