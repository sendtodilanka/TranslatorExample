@file:OptIn(ExperimentalCoroutinesApi::class)

package com.codebxlk.compose.translator.viewmodel

import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertHeaderItem
import com.codebxlk.compose.translator.data.DataStoreManager
import com.codebxlk.compose.translator.data.DataStoreManager.Companion.sourceKey
import com.codebxlk.compose.translator.data.DataStoreManager.Companion.targetKey
import com.codebxlk.compose.translator.data.model.ItemLanguageAdapter
import com.codebxlk.compose.translator.data.model.ItemLanguageAdapter.Item
import com.codebxlk.compose.translator.data.model.Language
import com.codebxlk.compose.translator.data.model.LanguageState
import com.codebxlk.compose.translator.data.model.LanguageState.DOWNLOADED
import com.codebxlk.compose.translator.data.model.LanguageState.DOWNLOADING
import com.codebxlk.compose.translator.data.model.LanguageState.SUPPORTED
import com.codebxlk.compose.translator.data.model.SelectedType
import com.codebxlk.compose.translator.data.repository.Repository
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

class LanguageViewModel @AssistedInject constructor(
    @Assisted val selectedTypeArgs: String,
    private val repository: Repository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val defaultAuto = Language(
        languageId = "auto",
        languageName = "Detect language",
        time = 0L,
        languageState = LanguageState.AUTO
    )

    val defaultSource = Language(
        languageId = "en",
        languageName = "English",
        time = 0L,
        languageState = DOWNLOADED
    )

    val defaultTarget = Language(
        languageId = "fr",
        languageName = "French",
        time = 0L,
        languageState = SUPPORTED
    )

    // Find whether the request came from source or target
    private val isFromSource = selectedTypeArgs == SelectedType.SOURCE.name

    // Get search results. if it is empty then get full list
    private val searchTerm: MutableStateFlow<String> = MutableStateFlow("")
    fun setSearchTerm(string: String) {
        searchTerm.value = string
    }

    /** Find the selected languages */
    val sourceLanguage = dataStoreManager.readValue(if (isFromSource) sourceKey else targetKey)
        .mapNotNull { getLanguageById(it) }
        .flowOn(Dispatchers.IO)

    val targetLanguage = dataStoreManager.readValue(if (isFromSource) targetKey else sourceKey)
        .mapNotNull { getLanguageById(it) }
        .flowOn(Dispatchers.IO)

    private suspend fun getLanguageById(languageId: String?): Language? {
        return languageId?.let {
            if (it == "auto") defaultAuto else repository.findLanguageById(it)
        }
    }

    val languageList = searchTerm.flatMapLatest {
        if (it.isNotBlank()) findLanguageByName(it) else findLanguagesWithRecent()
    }.cachedIn(viewModelScope).flowOn(Dispatchers.IO)

    private fun findLanguagesWithRecent(): Flow<PagingData<ItemLanguageAdapter>> {
        return repository.findLanguagesWithRecent().mapLatest { pagingData ->
            pagingData
                .let { if (isFromSource) it.insertHeaderItem(item = Item(defaultAuto)) else it }
            //.addBannerAds(totalAdCount = 2, isPremium = false) { count -> BannerAd(count) }
        }.flowOn(Dispatchers.IO)
    }

    private fun findLanguageByName(languageName: String): Flow<PagingData<ItemLanguageAdapter>> {
        return repository.findLanguageByName(languageName).flowOn(Dispatchers.IO)
    }

    fun swapLanguage(isFromSource: Boolean, sourceLanguageId: String, targetLanguageId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.dataStore.edit {
                it[if (isFromSource) targetKey else sourceKey] = sourceLanguageId
                it[if (isFromSource) sourceKey else targetKey] = targetLanguageId
            }
        }
    }

    fun saveLanguage(isFromSource: Boolean, languageId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.storeValue(
                key = if (isFromSource) sourceKey else targetKey,
                value = languageId
            )
        }
    }

    fun downloadLanguage(
        languageId: String,
        isWiFiRequired: Boolean,
        onSuccessListener: OnSuccessListener<in Void>,
        onFailureListener: OnFailureListener,
    ) {
        updateLanguageState(
            languageId = languageId,
            languageState = DOWNLOADING
        )

        repository.downloadModel(
            languageId = languageId,
            isWiFiRequired = isWiFiRequired,
            onSuccessListener = {
                updateLanguageState(
                    languageId = languageId,
                    languageState = DOWNLOADED
                )
                onSuccessListener.onSuccess(it)
            },
            onFailureListener = {
                updateLanguageState(
                    languageId = languageId,
                    languageState = SUPPORTED
                )
                onFailureListener.onFailure(it)
            }
        )
    }

    fun deleteLanguage(
        languageId: String,
        onSuccessListener: OnSuccessListener<in Void>,
        onFailureListener: OnFailureListener,
    ) {
        repository.removeLanguageModel(
            languageId = languageId,
            onSuccessListener = {
                updateLanguageState(
                    languageId = languageId,
                    languageState = SUPPORTED
                )
                onSuccessListener.onSuccess(it)
            },
            onFailureListener = {
                updateLanguageState(
                    languageId = languageId,
                    languageState = DOWNLOADED
                )
                onFailureListener.onFailure(it)
            }
        )
    }

    private fun updateLanguageState(languageId: String, languageState: LanguageState) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateLanguageState(languageId, languageState)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(selectedType: String): LanguageViewModel
    }

    companion object {
        fun provideFactory(
            factory: Factory,
            selectedType: String,
        ): ViewModelProvider.Factory {

            return object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return factory.create(selectedType) as T
                }
            }
        }
    }
}