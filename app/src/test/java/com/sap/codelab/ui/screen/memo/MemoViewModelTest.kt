package com.sap.codelab.ui.screen.memo

import com.sap.codelab.data.model.GeofenceRegisterResult
import com.sap.codelab.data.model.Memo
import com.sap.codelab.ui.screen.FakeGeofenceRepository
import com.sap.codelab.ui.screen.FakeMemoRepository
import com.sap.codelab.ui.screen.MainDispatcherRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

class MemoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val existing = Memo(
        id = 7,
        title = "Pharmacy",
        description = "Vitamins",
        reminderLatitude = 42.6977,
        reminderLongitude = 23.3219,
    )

    private val memoRepository = FakeMemoRepository(listOf(existing))
    private val geofenceRepository = FakeGeofenceRepository()

    private fun viewModel(launchMode: MemoLaunchMode) = MemoViewModel(
        memoRepository = memoRepository,
        geofenceRepository = geofenceRepository,
        launchMode = launchMode,
    )

    private fun MemoViewModel.saveValid() = onSaveMemo(
        title = "Groceries",
        description = "Milk and bread",
        latitude = 43.2141,
        longitude = 27.9147,
    )

    // region Loading

    @Test
    fun `create mode starts initialized with an empty memo`() {
        val state = viewModel(MemoLaunchMode.Create).uiState.value

        assertTrue(state.isInitialized)
        assertFalse(state.isEditing)
        assertEquals(Memo(), state.memo)
    }

    @Test
    fun `view mode loads the stored memo`() {
        val state = viewModel(MemoLaunchMode.View(existing.id)).uiState.value

        assertTrue(state.isInitialized)
        assertTrue(state.isEditing)
        assertEquals(existing, state.memo)
    }

    // endregion

    // region Creating

    @Test
    fun `saving a new memo stores it and registers its geofence with the new id`() {
        val viewModel = viewModel(MemoLaunchMode.Create)

        viewModel.saveValid()

        val saved = memoRepository.stored.single { it.title == "Groceries" }
        val registered = geofenceRepository.registeredMemos.single()
        assertEquals(saved.id, registered.id)
        assertTrue(registered.id > 0)
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `saving still completes when the geofence fails`() {
        geofenceRepository.result = GeofenceRegisterResult.MissingPermission
        val viewModel = viewModel(MemoLaunchMode.Create)

        viewModel.saveValid()

        assertTrue(viewModel.uiState.value.isSaved)
        assertEquals(2, memoRepository.stored.size)
    }

    @Test
    fun `an invalid memo is not saved`() {
        val viewModel = viewModel(MemoLaunchMode.Create)

        viewModel.onSaveMemo(
            title = "Groceries",
            description = "Milk and bread",
            latitude = Double.NEGATIVE_INFINITY,
            longitude = Double.NEGATIVE_INFINITY,
        )

        assertEquals(0, memoRepository.saveCount)
        assertTrue(geofenceRepository.registeredMemos.isEmpty())
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `an empty title is not saved`() {
        val viewModel = viewModel(MemoLaunchMode.Create)

        viewModel.onSaveMemo("", "Milk and bread", 43.2141, 27.9147)

        assertEquals(0, memoRepository.saveCount)
    }

    @Test
    fun `saving again after a successful save is ignored`() {
        val viewModel = viewModel(MemoLaunchMode.Create)

        viewModel.saveValid()
        viewModel.saveValid()

        assertEquals(1, memoRepository.saveCount)
        assertEquals(1, geofenceRepository.registeredMemos.size)
    }

    // endregion

    // region Editing

    @Test
    fun `editing keeps the id and updates the stored memo`() {
        val viewModel = viewModel(MemoLaunchMode.View(existing.id))

        viewModel.onSaveMemo("Pharmacy", "Vitamins and plasters", 42.70, 23.32)

        val updated = memoRepository.stored.single()
        assertEquals(existing.id, updated.id)
        assertEquals("Vitamins and plasters", updated.description)
        assertEquals(42.70, updated.reminderLatitude, 0.0)
    }

    @Test
    fun `editing re-registers the geofence under the same id`() {
        val viewModel = viewModel(MemoLaunchMode.View(existing.id))

        viewModel.onSaveMemo("Pharmacy", "Vitamins", 42.70, 23.32)

        val registered = geofenceRepository.registeredMemos.single()
        assertEquals(existing.id, registered.id)
        assertEquals(23.32, registered.reminderLongitude, 0.0)
    }

    /** Fails until onSaveMemo skips registration for closed memos (review item). */
    @Test
    fun `editing a closed memo does not register a geofence`() {
        val closed = existing.copy(id = 8, isDone = true)
        val repository = FakeMemoRepository(listOf(closed))
        val viewModel = MemoViewModel(
            memoRepository = repository,
            geofenceRepository = geofenceRepository,
            launchMode = MemoLaunchMode.View(closed.id),
        )

        viewModel.onSaveMemo("Pharmacy", "Vitamins", 42.70, 23.32)

        assertTrue(repository.stored.single().isDone)
        assertTrue(geofenceRepository.registeredMemos.isEmpty())
    }

    // endregion
}
