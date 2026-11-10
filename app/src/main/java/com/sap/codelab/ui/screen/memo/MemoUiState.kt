package com.sap.codelab.ui.screen.memo

import com.sap.codelab.data.model.Memo

/**
 * Represents the current state of the Memo screen.
 */
data class MemoUiState(
    /**
     * The memo the details of which are displayed on the screen.
     */
    val memo: Memo = Memo(),

    /**
     * Whether the state UI state has been fully initialized
     * (i.e., the stored memo has been retrieved).
     */
    val isInitialized: Boolean = false,

    /**
     * Whether the memo was saved/updated.
     */
    val isSaved: Boolean = false,

    /**
     * Whether the [memo] is to be edited, or we're creating a new memo.
     */
    val isEditing: Boolean = false,
)
