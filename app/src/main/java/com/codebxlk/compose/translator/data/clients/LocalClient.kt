package com.codebxlk.compose.translator.data.clients

import androidx.paging.PagingSource
import com.codebxlk.compose.translator.data.dao.LanguageDao
import com.codebxlk.compose.translator.data.model.Language
import com.codebxlk.compose.translator.data.model.LanguageState

class LocalClient(
    private val languageDao: LanguageDao
) {

    suspend fun updateLanguageState(languageId: String, languageState: LanguageState) {
        languageDao.updateLanguageState(languageId, languageState)
    }

    fun findLanguagesPaged(): PagingSource<Int, Language> {
        return languageDao.findLanguagesPaged()
    }

    fun findLanguagesWithRecent(): PagingSource<Int, Language> {
        return languageDao.findLanguagesWithRecent()
    }

    fun findLanguageByNamePaged(languageName: String): PagingSource<Int, Language> {
        return languageDao.findLanguageByName(languageName)
    }

    suspend fun findLanguageById(languageId: String?): Language? {
        return languageDao.findLanguageById(languageId)
    }
}