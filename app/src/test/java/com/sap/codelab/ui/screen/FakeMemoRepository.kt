package com.sap.codelab.ui.screen

import com.sap.codelab.data.model.Memo
import com.sap.codelab.data.repository.MemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [MemoRepository] that mirrors the DAO's filtering:
 * removed memos never appear, and "open" also excludes done memos.
 */
class FakeMemoRepository(initial: List<Memo> = emptyList()) : MemoRepository {

    private val memos = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

    /** Number of times a save method was called. */
    var saveCount = 0
        private set

    /** Every stored memo, including removed ones, for assertions. */
    val stored: List<Memo> get() = memos.value

    override fun getAllMemos(): Flow<List<Memo>> =
        memos.map { list -> list.filterNot { it.isRemoved }.sortedBy { it.id } }

    override fun getOpenMemos(): Flow<List<Memo>> =
        memos.map { list -> list.filterNot { it.isRemoved || it.isDone }.sortedBy { it.id } }

    override suspend fun getMemoById(id: Long): Memo? = memos.value.firstOrNull { it.id == id }

    override suspend fun saveMemo(memo: Memo): Long {
        saveCount++
        require(memo.isValid) { "An invalid memo reached the repository layer: $memo" }
        return upsert(memo)
    }

    override suspend fun saveMemos(memos: List<Memo>) {
        saveCount++
        memos.forEach { require(it.isValid) { "An invalid memo reached the repository layer: $it" } }
        memos.forEach(::upsert)
    }

    override suspend fun removeMemos(memos: List<Memo>) {
        val ids = memos.map { it.id }.toSet()
        this.memos.value = this.memos.value.filterNot { it.id in ids }
    }

    private fun upsert(memo: Memo): Long {
        val id = if (memo.id == 0L) nextId++ else memo.id
        memos.value = memos.value.filterNot { it.id == id } + memo.copy(id = id)
        return id
    }
}
