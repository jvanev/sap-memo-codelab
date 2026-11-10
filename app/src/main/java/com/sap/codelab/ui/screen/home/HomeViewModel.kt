package com.sap.codelab.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.data.model.Memo
import com.sap.codelab.data.repository.MemoRepository
import com.sap.codelab.data.repository.impl.MemoRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * The ViewModel accompanying [HomeScreen], and holder of [HomeUiState].
 */
class HomeViewModel(
    private val memoRepository: MemoRepository = MemoRepositoryImpl,
) : ViewModel() {
    private val showAllMemos = MutableStateFlow(false)
    private val undoableAction = MutableStateFlow<UndoableMemoAction?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = showAllMemos
        .flatMapLatest { showsAllMemos ->
            val memosFlow = if (showsAllMemos) {
                memoRepository.getAllMemos()
            } else {
                memoRepository.getOpenMemos()
            }

            memosFlow.map { memos -> HomeUiState(memos = memos, showsAllMemos = showsAllMemos) }
        }
        .combine(undoableAction) { uiState, undoableAction ->
            uiState.copy(undoableAction = undoableAction)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), HomeUiState())

    /**
     * Toggles between all/open memos list.
     */
    fun toggleMemosListMode() {
        showAllMemos.value = !showAllMemos.value
    }

    /**
     * Closes the specified [memo] and registers it as an undoable action that can be reverted.
     */
    fun closeMemo(memo: Memo) {
        viewModelScope.launch {
            memoRepository.saveMemo(memo.copy(isDone = true))
            undoableAction.update { UndoableMemoAction.Close(memo) }
        }
    }

    /**
     * Removes the specified [memo] and registers it as an undoable action that can be reverted.
     */
    fun removeMemo(memo: Memo) {
        viewModelScope.launch {
            memoRepository.saveMemo(memo.copy(isRemoved = true))
            undoableAction.update { UndoableMemoAction.Remove(memo) }
        }
    }

    /**
     * Restores the previous state of the pending undoable action.
     */
    fun undoAction() {
        undoableAction.value?.let { undoneAction ->
            viewModelScope.launch {
                memoRepository.saveMemos(undoneAction.memos)
                undoableAction.update { action ->
                    if (action == undoneAction) {
                        when (action) {
                            is UndoableMemoAction.Close -> action.copy(
                                memos = emptyList(),
                                undone = true,
                            )

                            is UndoableMemoAction.Remove -> action.copy(
                                memos = emptyList(),
                                undone = true,
                            )
                        }
                    } else {
                        action
                    }
                }
            }
        }
    }

    /**
     * Clears the undoable action. If the action wasn't undone, it's effects become permanent.
     */
    fun clearAction() {
        undoableAction.update { null }
    }
}
