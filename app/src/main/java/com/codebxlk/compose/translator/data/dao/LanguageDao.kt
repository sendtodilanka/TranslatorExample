package com.codebxlk.compose.translator.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.codebxlk.compose.translator.data.model.Language
import com.codebxlk.compose.translator.data.model.LanguageState

@Dao
interface LanguageDao {

    @Query("UPDATE Language SET offlineState = :languageState WHERE languageId = :languageId")
    suspend fun updateLanguageState(languageId: String, languageState: LanguageState)

    @Query("SELECT * FROM language ORDER BY languageName ASC")
    fun findLanguagesPaged(): PagingSource<Int, Language>

    @Query("SELECT * FROM language WHERE languageName LIKE :languageName || '%' ORDER BY languageName ASC")
    fun findLanguageByName(languageName: String): PagingSource<Int, Language>

    @Query(
        """
            SELECT *
            FROM language
            ORDER BY
            CASE
                WHEN time > 0 THEN time
                ELSE NULL
            END DESC,
            languageName ASC
        """
    )
    fun findLanguagesWithRecent(): PagingSource<Int, Language>

    @Query("SELECT * FROM language WHERE languageId = :languageId")
    suspend fun findLanguageById(languageId: String?): Language?
}