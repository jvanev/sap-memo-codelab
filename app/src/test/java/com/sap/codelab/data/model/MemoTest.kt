package com.sap.codelab.data.model

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class MemoTest {

    private val valid = Memo(
        title = "Groceries",
        description = "Milk and bread",
        reminderLatitude = 43.2141,
        reminderLongitude = 27.9147,
    )

    @Test
    fun `memo with title, description and position is valid`() {
        assertTrue(valid.isValid)
        assertTrue(valid.isValidReminderPosition)
    }

    @Test
    fun `default memo has no valid position`() {
        assertFalse(Memo().isValidReminderPosition)
        assertFalse(Memo().isValid)
    }

    @Test
    fun `empty title is invalid`() {
        assertFalse(valid.copy(title = "").isValid)
    }

    @Test
    fun `empty description is invalid`() {
        assertFalse(valid.copy(description = "").isValid)
    }

    @Test
    fun `missing latitude or longitude is invalid`() {
        assertFalse(valid.copy(reminderLatitude = Double.NEGATIVE_INFINITY).isValid)
        assertFalse(valid.copy(reminderLongitude = Double.NEGATIVE_INFINITY).isValid)
    }

    /** Fails until Memo.isValid uses isNotBlank() (review item). */
    @Test
    fun `whitespace-only title or description is invalid`() {
        assertFalse(valid.copy(title = "   ").isValid)
        assertFalse(valid.copy(description = "\n ").isValid)
    }

    @Test
    fun `coordinates at zero are a valid position`() {
        assertTrue(
            valid.copy(
                reminderLatitude = 0.0,
                reminderLongitude = 0.0,
            ).isValidReminderPosition,
        )
    }
}
