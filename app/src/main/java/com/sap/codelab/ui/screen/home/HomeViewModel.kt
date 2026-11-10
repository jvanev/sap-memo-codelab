package com.sap.codelab.ui.screen.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The ViewModel accompanying [HomeScreen], and holder of [HomeUiState].
 */
class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Toggles between showing all/open memos.
     */
    fun toggleMemosListMode() {
        _uiState.update { it.copy(showsAllMemos = !it.showsAllMemos) }
    }
}
