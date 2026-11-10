package com.sap.codelab.data.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Represents an individual memo.
 */
@Entity(tableName = "memos")
data class Memo(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "reminder_date") val reminderDate: Long = 0,
    @ColumnInfo(name = "reminder_latitude") val reminderLatitude: Double = INVALID_COORDINATES,
    @ColumnInfo(name = "reminder_longitude") val reminderLongitude: Double = INVALID_COORDINATES,
    @ColumnInfo(name = "is_done") val isDone: Boolean = false,
) {
    /**
     * Whether this memo's coordinates are valid.
     */
    val isValidReminderPosition
        get() = reminderLatitude != INVALID_COORDINATES && reminderLongitude != INVALID_COORDINATES
}

/**
 * Represents invalid coordinates
 */
private const val INVALID_COORDINATES = Double.NEGATIVE_INFINITY
