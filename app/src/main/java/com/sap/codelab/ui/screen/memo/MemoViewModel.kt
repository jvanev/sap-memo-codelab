package com.sap.codelab.ui.screen.memo

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The ViewModel accompanying [MemoScreen], and holder of [MemoUiState].
 *
 * @param launchMode The operational mode of this ViewModel
 */
class MemoViewModel(launchMode: MemoLaunchMode) : ViewModel() {
    private val _uiState = MutableStateFlow(MemoUiState())
    val uiState: StateFlow<MemoUiState> = _uiState.asStateFlow()

    init {
        if (launchMode is MemoLaunchMode.View) {
            // TODO: Fetch the memo by id
        }
    }

    /**
     * Saves the memo with the specified details.
     */
    fun onSaveMemo(title: String, description: String, latitude: Double, longitude: Double) {
        // TODO: Save the memo details
    }
}
