package com.sap.codelab.ui.screen.home

import com.sap.codelab.data.model.Memo

/**
 * Represents the current state of the home screen.
 */
data class HomeUiState(
    /**
     * The list of memos displayed on the screen.
     */
    val memos: List<Memo> = emptyList(),

    /**
     * Whether to show all stored memos, or only memos not marked as "done".
     */
    val showsAllMemos: Boolean = false,
)
