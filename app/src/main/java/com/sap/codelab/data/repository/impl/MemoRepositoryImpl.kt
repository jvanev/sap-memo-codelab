package com.sap.codelab.data.repository.impl

import android.content.Context
import androidx.room3.Room
import com.sap.codelab.data.database.Database
import com.sap.codelab.data.model.Memo
import com.sap.codelab.data.repository.MemoRepository
import kotlinx.coroutines.flow.Flow

/**
 * The singleton implementation of the [MemoRepository] interface.
 *
 * Make sure [initialize] is called before this object reaches the higher-levels of the application.
 */
object MemoRepositoryImpl : MemoRepository {
    private lateinit var database: Database

    override fun getAllMemos(): Flow<List<Memo>> = database.memoDao().getAll()

    override fun getOpenMemos(): Flow<List<Memo>> = database.memoDao().getOpen()

    override suspend fun getMemoById(id: Long): Memo? = database.memoDao().getMemoById(id)

    override suspend fun saveMemo(memo: Memo): Long {
        if (!memo.isValid) {
            throw IllegalStateException("An invalid memo reached the repository layer: $memo")
        }

        return database.memoDao().upsert(memo)
    }

    override suspend fun removeMemo(memo: Memo) = database.memoDao().delete(memo)

    override suspend fun removeMemo(memos: List<Memo>) = database.memoDao().delete(memos)

    override suspend fun removeMemo(id: Long) = database.memoDao().delete(id)

    /**
     * Initializes this repository.
     */
    fun initialize(context: Context) {
        if (!::database.isInitialized) {
            database = Room.databaseBuilder(context, Database::class.java, DATABASE_NAME)
                .build()
        }
    }
}

/**
 * The name of the SQLite database.
 */
private const val DATABASE_NAME = "memo_codelab"
