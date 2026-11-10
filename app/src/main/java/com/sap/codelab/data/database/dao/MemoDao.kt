package com.sap.codelab.data.database.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import com.sap.codelab.data.model.Memo
import kotlinx.coroutines.flow.Flow

/**
 * Provides access to [Memo]-related operations in the database.
 */
@Dao
interface MemoDao {
    /**
     * @return All memos currently stored in the database.
     */
    @Query("SELECT * FROM memos WHERE is_removed = 0 ORDER BY id ASC")
    fun getAll(): Flow<List<Memo>>

    /**
     * @return All memos currently stored in the database that have not yet been marked as "done".
     */
    @Query("SELECT * FROM memos WHERE is_done = 0 AND is_removed = 0 ORDER BY id ASC")
    fun getOpen(): Flow<List<Memo>>

    /**
     * @return The memo whose [Memo.id] matches the specified [id],
     *         or `null` if a memo with that id doesn't exist.
     */
    @Query("SELECT * FROM memos WHERE id = :id")
    suspend fun getMemoById(id: Long): Memo?

    /**
     * Stores the details of the specified [memo] into the database.
     *
     * If the memo already exists, its details will be updated,
     * otherwise a new database entry will be created.
     *
     * @return The id of the newly created [memo], or -1 if the memo already exists.
     */
    @Upsert
    suspend fun upsert(memo: Memo): Long

    /**
     * Stores the details of the specified [memos] into the database.
     *
     * If a memo already exists, its details will be updated,
     * otherwise a new database entry will be created.
     */
    @Upsert
    suspend fun upsert(memos: List<Memo>)

    /**
     * Deletes the specified [memos] from the database.
     */
    @Delete
    suspend fun delete(memos: List<Memo>)
}
