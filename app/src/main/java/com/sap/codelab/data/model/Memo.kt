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
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "reminder_date") val reminderDate: Long,
    @ColumnInfo(name = "reminder_latitude") val reminderLatitude: Double,
    @ColumnInfo(name = "reminder_longitude") val reminderLongitude: Double,
    @ColumnInfo(name = "is_done") val isDone: Boolean = false,
)
