package com.sap.codelab.ui.screen.memo

/**
 * Implementations of this interface represent the different modes
 * in which the MemoScreen can be launched.
 */
sealed interface MemoLaunchMode {
    /**
     * Launch mode for creating a new memo.
     */
    data object Create : MemoLaunchMode

    /**
     * Launch mode for displaying the details of an existing memo with the specified [memoId].
     */
    data class View(val memoId: Long) : MemoLaunchMode
}
