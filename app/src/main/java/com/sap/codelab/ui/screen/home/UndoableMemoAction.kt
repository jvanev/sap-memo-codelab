package com.sap.codelab.ui.screen.home

import com.sap.codelab.data.model.Memo

/**
 * Implementations of this interface represent undoable actions that allow
 * memos' previous state to be restored.
 */
sealed interface UndoableMemoAction {
    /**
     * The memos which can be restored to their previous state.
     */
    val memos: List<Memo>

    /**
     * Whether the previous state of the memos affected by this action was restored.
     */
    val undone: Boolean

    /**
     * Represents an intent to close the specified [memos].
     */
    data class Close(
        override val memos: List<Memo>,
        override val undone: Boolean = false,
    ) : UndoableMemoAction {

        /**
         * Creates a new [Close] instance for the specified [memo].
         */
        constructor(memo: Memo) : this(listOf(memo))
    }

    /**
     * Represents an intent to remove the specified [memos].
     */
    data class Remove(
        override val memos: List<Memo>,
        override val undone: Boolean = false,
    ) : UndoableMemoAction {

        /**
         * Creates a new [Remove] instance for the specified [memo].
         */
        constructor(memo: Memo) : this(listOf(memo))
    }
}
