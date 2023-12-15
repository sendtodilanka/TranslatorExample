package com.codebxlk.compose.translator.data.repository

import androidx.annotation.WorkerThread
import androidx.paging.PagingData
import com.codebxlk.compose.translator.data.model.ItemLanguageAdapter
import com.codebxlk.compose.translator.data.model.Language
import com.codebxlk.compose.translator.data.model.LanguageState
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.flow.Flow

interface Repository {

    @WorkerThread
    suspend fun updateLanguageState(languageId: String, languageState: LanguageState)

    @WorkerThread
    fun findLanguagesPaged(): Flow<PagingData<Language>>

    @WorkerThread
    fun findLanguagesWithRecent(): Flow<PagingData<Language>>

    @WorkerThread
    fun findLanguageByName(languageName: String): Flow<PagingData<Language>>

    @WorkerThread
    suspend fun findLanguageById(languageId: String?): Language?


    fun downloadModel(
        languageId: String,
        isWiFiRequired: Boolean,
        onSuccessListener: OnSuccessListener<in Void>,
        onFailureListener: OnFailureListener,
    )

    fun removeLanguageModel(
        languageId: String,
        onSuccessListener: OnSuccessListener<in Void>,
        onFailureListener: OnFailureListener,
    )
}