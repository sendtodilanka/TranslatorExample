package com.codebxlk.compose.translator.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import com.codebxlk.compose.translator.data.clients.LocalClient
import com.codebxlk.compose.translator.data.clients.MLKitClient
import com.codebxlk.compose.translator.data.model.ItemLanguageAdapter
import com.codebxlk.compose.translator.data.model.ItemLanguageAdapter.Item
import com.codebxlk.compose.translator.data.model.ItemLanguageAdapter.SectionTitle
import com.codebxlk.compose.translator.data.model.Language
import com.codebxlk.compose.translator.data.model.LanguageState
import com.codebxlk.compose.translator.data.model.LanguageState.DOWNLOADED
import com.codebxlk.compose.translator.data.model.LanguageState.DOWNLOADING
import com.codebxlk.compose.translator.data.model.LanguageState.NONE
import com.codebxlk.compose.translator.data.model.LanguageState.SUPPORTED
import com.codebxlk.compose.translator.di.Dispatcher
import com.codebxlk.compose.translator.di.IoDispatchers
import com.codebxlk.compose.translator.data.repository.Repository
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val localClient: LocalClient,
    private val mlKitClient: MLKitClient,
    @Dispatcher(IoDispatchers.IO) private val ioDispatchers: CoroutineDispatcher
) : Repository {

    private fun PagingData<Language>.processPagingData(
        downloadedModelIds: Set<String>,
    ): PagingData<Language> {
        return map { language ->
            val isOfflineSupported = mlKitClient.offlineLanguages.contains(language.languageId)
            val isModelDownloaded = downloadedModelIds.contains(language.languageId)

            val offlineStatus = when {
                language.languageState == DOWNLOADING && !isModelDownloaded -> DOWNLOADING
                isModelDownloaded -> DOWNLOADED
                isOfflineSupported -> SUPPORTED
                else -> NONE
            }

            language.copy(languageState = offlineStatus)
        }
    }

    private suspend fun PagingData<ItemLanguageAdapter>.insertSeparators(): PagingData<ItemLanguageAdapter> {
        return withContext(ioDispatchers) {
            insertSeparators { before, after ->
                when {
                    before == null && after != null -> {
                        when {
                            after is Item && after.language.time > 0L -> SectionTitle(
                                "Recent Languages"
                            )
                            after is Item && after.language.time == 0L -> SectionTitle(
                                "All Other Languages"
                            )
                            else -> null
                        }
                    }

                    before is Item && before.language.time > 0L && after is Item && after.language.time == 0L -> {
                        SectionTitle("All Languages")
                    }

                    else -> null
                }
            }
        }
    }

    override suspend fun updateLanguageState(languageId: String, languageState: LanguageState) {
        localClient.updateLanguageState(languageId, languageState)
    }

    override fun findLanguagesPaged(): Flow<PagingData<Language>> {
        return Pager(config = PagingConfig(pageSize = 15, enablePlaceholders = false)) {
            localClient.findLanguagesPaged()
        }.flow.flowOn(ioDispatchers)
    }

    override fun findLanguagesWithRecent(): Flow<PagingData<Language>> {
        return Pager(config = PagingConfig(pageSize = 15, enablePlaceholders = false)) {
            localClient.findLanguagesWithRecent()
        }.flow.map { pagingData ->
            val downloadedModelIds = mlKitClient.getDownloadedModelIds()
            pagingData.processPagingData(downloadedModelIds)
        }.flowOn(ioDispatchers)
    }

    override fun findLanguageByName(languageName: String): Flow<PagingData<Language>> {
        return Pager(config = PagingConfig(pageSize = 15, enablePlaceholders = false)) {
            localClient.findLanguageByNamePaged(languageName)
        }.flow.map { pagingData ->
            val downloadedModelIds = mlKitClient.getDownloadedModelIds()
            pagingData.processPagingData(downloadedModelIds)
        }.flowOn(ioDispatchers)
    }

    override suspend fun findLanguageById(languageId: String?): Language? {
        return localClient.findLanguageById(languageId)
    }

    override fun downloadModel(
        languageId: String,
        isWiFiRequired: Boolean,
        onSuccessListener: OnSuccessListener<in Void>,
        onFailureListener: OnFailureListener
    ) {
        mlKitClient.downloadModel(
            languageId,
            isWiFiRequired,
            onSuccessListener,
            onFailureListener
        )
    }

    override fun removeLanguageModel(
        languageId: String,
        onSuccessListener: OnSuccessListener<in Void>,
        onFailureListener: OnFailureListener
    ) {
        mlKitClient.removeLanguageModel(
            languageId,
            onSuccessListener,
            onFailureListener
        )
    }
}