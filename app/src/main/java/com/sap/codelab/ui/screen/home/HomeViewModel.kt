package com.sap.codelab.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.data.model.Memo
import com.sap.codelab.data.repository.GeofenceRepository
import com.sap.codelab.data.repository.MemoRepository
import com.sap.codelab.data.repository.PreferencesRepository
import com.sap.codelab.data.repository.impl.GeofenceRepositoryImpl
import com.sap.codelab.data.repository.impl.MemoRepositoryImpl
import com.sap.codelab.data.repository.impl.PreferencesRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * The ViewModel accompanying [HomeScreen], and holder of [HomeUiState].
 */
class HomeViewModel(
    private val memoRepository: MemoRepository = MemoRepositoryImpl,
    private val geofenceRepository: GeofenceRepository = GeofenceRepositoryImpl,
    private val preferencesRepository: PreferencesRepository = PreferencesRepositoryImpl,
) : ViewModel() {

    private val showAllMemos = MutableStateFlow(false)
    private val undoableAction = MutableStateFlow<UndoableMemoAction?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val memos: Flow<List<Memo>> = showAllMemos
        .flatMapLatest { showsAllMemos ->
            if (showsAllMemos) {
                memoRepository.getAllMemos()
            } else {
                memoRepository.getOpenMemos()
            }
        }

    val uiState: StateFlow<HomeUiState> = combine(
        flow = preferencesRepository.getPreferences(),
        flow2 = memos,
        flow3 = showAllMemos,
        flow4 = undoableAction,
    ) { preferences, memos, showsAllMemos, undoableAction ->

        HomeUiState(
            preferences = preferences,
            memos = memos,
            showsAllMemos = showsAllMemos,
            undoableAction = undoableAction,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), HomeUiState())

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
            geofenceRepository.removeGeofence(memo)
            undoableAction.update { UndoableMemoAction.Close(memo) }
        }
    }

    /**
     * Removes the specified [memo] and registers it as an undoable action that can be reverted.
     */
    fun removeMemo(memo: Memo) {
        viewModelScope.launch {
            memoRepository.saveMemo(memo.copy(isDone = true, isRemoved = true))
            geofenceRepository.removeGeofence(memo)
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
                geofenceRepository.addGeofences(undoneAction.memos.filter { !it.isDone })
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

    /**
     * Stores the user's request not to see prompts for foreground location permission.
     */
    fun denyFineLocationPermission() {
        viewModelScope.launch {
            preferencesRepository.denyFineLocationRequests()
        }
    }

    /**
     * Stores the user's request not to see prompts for background location permission.
     */
    fun denyBackgroundLocationPermission() {
        viewModelScope.launch {
            preferencesRepository.denyBackgroundLocationRequests()
        }
    }

    /**
     * Stores the user's request not to see prompts for notifications permission.
     */
    fun denyNotificationPermission() {
        viewModelScope.launch {
            preferencesRepository.denyNotificationRequests()
        }
    }
}
