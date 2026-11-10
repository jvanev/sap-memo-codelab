package com.sap.codelab.ui.screen.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.data.repository.MemoRepository
import com.sap.codelab.data.repository.impl.MemoRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The ViewModel accompanying [MemoScreen], and holder of [MemoUiState].
 *
 * @param launchMode The operational mode of this ViewModel
 */
class MemoViewModel(
    private val memoRepository: MemoRepository = MemoRepositoryImpl,
    launchMode: MemoLaunchMode,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MemoUiState())
    val uiState: StateFlow<MemoUiState> = _uiState.asStateFlow()

    init {
        if (launchMode is MemoLaunchMode.View) {
            viewModelScope.launch {
                val memo = memoRepository.getMemoById(launchMode.memoId)

                if (memo != null) {
                    _uiState.value = MemoUiState(
                        memo = memo,
                        isInitialized = true,
                        isEditing = true,
                    )
                }
            }
        } else {
            _uiState.update { it.copy(isInitialized = true) }
        }
    }

    /**
     * Saves the memo with the specified details.
     */
    fun onSaveMemo(title: String, description: String, latitude: Double, longitude: Double) {
        if (_uiState.value.isSaved) {
            return
        }

        val memo = _uiState.value.memo.copy(
            title = title,
            description = description,
            reminderLatitude = latitude,
            reminderLongitude = longitude,
        )

        if (memo.isValid) {
            viewModelScope.launch {
                memoRepository.saveMemo(memo)
                _uiState.update { it.copy(isSaved = true) }
            }
        }
    }
}
