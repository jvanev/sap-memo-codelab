package com.sap.codelab.data.repository

import com.sap.codelab.data.model.Memo
import kotlinx.coroutines.flow.Flow

/**
 * Implementations of this interface provide [Memo]-related operations.
 */
interface MemoRepository {
    /**
     * Returns a [Flow] of all stored [Memo]s.
     */
    fun getAllMemos(): Flow<List<Memo>>

    /**
     * Returns a [Flow] of all stored [Memo]s that have not yet been marked as "done".
     */
    fun getOpenMemos(): Flow<List<Memo>>

    /**
     * @return The [Memo] with the specified [id], or `null` if no memo with that id exists.
     */
    suspend fun getMemoById(id: Long): Memo?

    /**
     * Stores the specified [memo].
     *
     * @return The [Memo.id] of the specified [memo].
     */
    suspend fun saveMemo(memo: Memo): Long

    /**
     * Stores the specified [memos].
     */
    suspend fun saveMemos(memos: List<Memo>)

    /**
     * Removes the specified [memos].
     */
    suspend fun removeMemos(memos: List<Memo>)
}
