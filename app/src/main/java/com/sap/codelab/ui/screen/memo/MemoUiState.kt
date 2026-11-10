package com.sap.codelab.ui.screen.memo

import com.sap.codelab.data.model.Memo

/**
 * Represents the current state of the Memo screen.
 */
data class MemoUiState(
    /**
     * The memo the details of which are displayed on the screen.
     */
    val memo: Memo = Memo(
        id = 0,
        title = "",
        description = "",
        reminderDate = 0,
        reminderLatitude = Double.NEGATIVE_INFINITY,
        reminderLongitude = Double.NEGATIVE_INFINITY,
    ),

    /**
     * Whether the [memo] is to be edited, or we're creating a new memo.
     */
    val isEditing: Boolean = false,
)
