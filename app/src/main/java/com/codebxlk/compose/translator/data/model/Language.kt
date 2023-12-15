package com.codebxlk.compose.translator.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Language(
    @PrimaryKey val languageId: String,
    val languageName: String,
    val time: Long = 0L,
    @ColumnInfo(name = "offlineState")
    var languageState: LanguageState
)

enum class LanguageState {
    NONE, AUTO, SUPPORTED, DOWNLOADING, DOWNLOADED
}

enum class SelectedType {
    SOURCE, TARGET
}