package com.codebxlk.compose.translator.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.codebxlk.compose.translator.data.DataStoreManager
import com.codebxlk.compose.translator.data.DatabaseManager
import com.codebxlk.compose.translator.data.clients.LocalClient
import com.codebxlk.compose.translator.data.clients.MLKitClient
import com.codebxlk.compose.translator.data.dao.LanguageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object LocalModule {

    private const val DBNAME: String = "translator.db"

    @Provides
    @Dispatcher(IoDispatchers.IO)
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideDataStoreManager(
        @ApplicationContext context: Context
    ): DataStoreManager = DataStoreManager(context)

    @Provides
    @Singleton
    fun provideAppDatabase(application: Application): DatabaseManager {
        return Room
            .databaseBuilder(application, DatabaseManager::class.java, DBNAME)
            .createFromAsset(DBNAME)
            .build()
    }

    @Provides
    @Singleton
    fun provideLanguageDao(databaseManager: DatabaseManager): LanguageDao {
        return databaseManager.languageDao()
    }



    @Provides
    @Singleton
    fun provideMLKitClient(application: Application): MLKitClient = MLKitClient(application)

    @Provides
    @Singleton
    fun provideLocalClient(
        languageDao: LanguageDao,
        /*translateDao: TranslateDao,
        collectionDao: CollectionDao,*/
    ): LocalClient = LocalClient(languageDao/*, translateDao, collectionDao*/)
}
