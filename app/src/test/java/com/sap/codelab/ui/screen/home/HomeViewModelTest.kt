package com.sap.codelab.ui.screen.home

import com.sap.codelab.data.model.Memo
import com.sap.codelab.ui.screen.FakeGeofenceRepository
import com.sap.codelab.ui.screen.FakeMemoRepository
import com.sap.codelab.ui.screen.FakePreferencesRepository
import com.sap.codelab.ui.screen.MainDispatcherRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val open = memo(id = 1, title = "Open")
    private val done = memo(id = 2, title = "Done", isDone = true)
    private val removed = memo(id = 3, title = "Removed", isRemoved = true)

    private lateinit var memoRepository: FakeMemoRepository
    private lateinit var geofenceRepository: FakeGeofenceRepository
    private lateinit var preferencesRepository: FakePreferencesRepository

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        memoRepository = FakeMemoRepository(listOf(open, done, removed))
        geofenceRepository = FakeGeofenceRepository()
        preferencesRepository = FakePreferencesRepository()
        viewModel = HomeViewModel(
            memoRepository = memoRepository,
            geofenceRepository = geofenceRepository,
            preferencesRepository = preferencesRepository,
        )
    }

    private fun memo(id: Long, title: String, isDone: Boolean = false, isRemoved: Boolean = false) =
        Memo(
            id = id,
            title = title,
            description = "Description of $title",
            reminderLatitude = 43.2141,
            reminderLongitude = 27.9147,
            isDone = isDone,
            isRemoved = isRemoved,
        )


    /** uiState uses WhileSubscribed, so it only updates while collected. */
    private fun TestScope.startCollecting() {
        backgroundScope.launch { viewModel.uiState.collect {} }
    }

    private fun visibleTitles() = viewModel.uiState.value.memos.map { it.title }

    // region Listing

    @Test
    fun `shows only open memos by default`() = runTest(mainDispatcherRule.dispatcher) {
        startCollecting()

        assertFalse(viewModel.uiState.value.showsAllMemos)
        assertEquals(listOf("Open"), visibleTitles())
    }

    @Test
    fun `toggling shows done memos but never removed ones`() =
        runTest(mainDispatcherRule.dispatcher) {
            startCollecting()

            viewModel.toggleMemosListMode()

            assertTrue(viewModel.uiState.value.showsAllMemos)
            assertEquals(listOf("Open", "Done"), visibleTitles())
        }

    @Test
    fun `toggling twice returns to open memos`() = runTest(mainDispatcherRule.dispatcher) {
        startCollecting()

        viewModel.toggleMemosListMode()
        viewModel.toggleMemosListMode()

        assertEquals(listOf("Open"), visibleTitles())
    }

    @Test
    fun `ui state carries the stored preferences`() = runTest(mainDispatcherRule.dispatcher) {
        startCollecting()

        assertEquals(preferencesRepository.stored, viewModel.uiState.value.preferences)
    }

    // endregion

    // region Close and remove

    @Test
    fun `closing marks the memo done and removes its geofence`() =
        runTest(mainDispatcherRule.dispatcher) {
            startCollecting()

            viewModel.closeMemo(open)

            assertTrue(memoRepository.getMemoById(open.id)!!.isDone)
            assertEquals(listOf(open.id), geofenceRepository.removedIds)
            assertTrue(visibleTitles().isEmpty())
        }

    @Test
    fun `closing offers an undo holding the original memo`() =
        runTest(mainDispatcherRule.dispatcher) {
            startCollecting()

            viewModel.closeMemo(open)

            val action = viewModel.uiState.value.undoableAction
            assertTrue(action is UndoableMemoAction.Close)
            assertEquals(listOf(open), action!!.memos)
            assertFalse(action.undone)
        }

    @Test
    fun `removing soft-deletes the memo and removes its geofence`() =
        runTest(mainDispatcherRule.dispatcher) {
            startCollecting()

            viewModel.removeMemo(open)

            assertTrue(memoRepository.getMemoById(open.id)!!.isRemoved)
            assertEquals(listOf(open.id), geofenceRepository.removedIds)
            assertTrue(viewModel.uiState.value.undoableAction is UndoableMemoAction.Remove)
        }

    // endregion

    // region Undo

    @Test
    fun `undoing a close reopens the memo and re-registers its geofence`() =
        runTest(mainDispatcherRule.dispatcher) {
            startCollecting()
            viewModel.closeMemo(open)

            viewModel.undoAction()

            assertFalse(memoRepository.getMemoById(open.id)!!.isDone)
            assertEquals(listOf(open.id), geofenceRepository.registeredMemos.map { it.id })
            assertEquals(listOf("Open"), visibleTitles())
            assertTrue(viewModel.uiState.value.undoableAction!!.undone)
        }

    @Test
    fun `undoing a removal restores the memo`() = runTest(mainDispatcherRule.dispatcher) {
        startCollecting()
        viewModel.removeMemo(open)

        viewModel.undoAction()

        assertFalse(memoRepository.getMemoById(open.id)!!.isRemoved)
        assertEquals(listOf("Open"), visibleTitles())
    }

    /** Fails until undoAction filters out done memos before registering (review item). */
    @Test
    fun `undoing the removal of a done memo does not register a geofence`() =
        runTest(mainDispatcherRule.dispatcher) {
            startCollecting()
            viewModel.removeMemo(done)

            viewModel.undoAction()

            assertTrue(memoRepository.getMemoById(done.id)!!.isDone)
            assertTrue(geofenceRepository.registeredMemos.isEmpty())
        }

    @Test
    fun `undo without a pending action does nothing`() = runTest(mainDispatcherRule.dispatcher) {
        startCollecting()

        viewModel.undoAction()

        assertEquals(0, memoRepository.saveCount)
        assertTrue(geofenceRepository.registeredMemos.isEmpty())
    }

    @Test
    fun `clearing the action removes it`() = runTest(mainDispatcherRule.dispatcher) {
        startCollecting()
        viewModel.closeMemo(open)

        viewModel.clearAction()

        assertNull(viewModel.uiState.value.undoableAction)
        assertTrue(memoRepository.getMemoById(open.id)!!.isDone)
    }

    // endregion

    // region Permission preferences

    @Test
    fun `denying fine location stores only that preference`() =
        runTest(mainDispatcherRule.dispatcher) {
            startCollecting()

            viewModel.denyFineLocationPermission()

            val stored = preferencesRepository.stored
            assertFalse(stored.allowsFineLocationRequests)
            assertTrue(stored.allowsBackgroundLocationRequests)
            assertTrue(stored.allowsNotificationRequests)
        }

    /**
     * Fails until the deny methods read the stored preferences instead of uiState.value
     * (review item): with no collector, uiState.value holds the all-false defaults.
     */
    @Test
    fun `denying a permission without a ui collector keeps the others`() =
        runTest(mainDispatcherRule.dispatcher) {
            viewModel.denyNotificationPermission()

            val stored = preferencesRepository.stored
            assertFalse(stored.allowsNotificationRequests)
            assertTrue(stored.allowsFineLocationRequests)
            assertTrue(stored.allowsBackgroundLocationRequests)
        }

    // endregion
}
