package com.codebxlk.compose.translator.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.codebxlk.compose.translator.data.dao.LanguageDao
import com.codebxlk.compose.translator.data.model.Language

@Database(
    entities = [Language::class],
    version = 6,
    exportSchema = true
)
abstract class DatabaseManager: RoomDatabase() {
    abstract fun languageDao(): LanguageDao
}