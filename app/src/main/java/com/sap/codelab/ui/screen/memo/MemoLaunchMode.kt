package com.sap.codelab.ui.screen.memo

import kotlinx.serialization.Serializable

/**
 * Implementations of this interface represent the different modes
 * in which the MemoScreen can be launched.
 */
@Serializable
sealed interface MemoLaunchMode {
    /**
     * Launch mode for creating a new memo.
     */
    @Serializable
    data object Create : MemoLaunchMode

    /**
     * Launch mode for displaying the details of an existing memo with the specified [memoId].
     */
    @Serializable
    data class View(val memoId: Long) : MemoLaunchMode
}
