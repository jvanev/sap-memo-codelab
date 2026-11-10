package com.sap.codelab.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.data.repository.MemoRepository
import com.sap.codelab.data.repository.impl.MemoRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

/**
 * The ViewModel accompanying [HomeScreen], and holder of [HomeUiState].
 */
class HomeViewModel(
    private val memoRepository: MemoRepository = MemoRepositoryImpl,
) : ViewModel() {
    private val showAllMemos = MutableStateFlow(false)

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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), HomeUiState())

    /**
     * Toggles between showing all/open memos.
     */
    fun toggleMemosListMode() {
        showAllMemos.value = !showAllMemos.value
    }
}
