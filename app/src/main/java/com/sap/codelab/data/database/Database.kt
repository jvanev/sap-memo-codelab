package com.sap.codelab.data.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.sap.codelab.data.database.dao.MemoDao
import com.sap.codelab.data.model.Memo

@Database(entities = [Memo::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {
    abstract fun memoDao(): MemoDao
}
