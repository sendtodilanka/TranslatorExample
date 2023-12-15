package com.codebxlk.compose.translator.di

import com.codebxlk.compose.translator.data.repository.Repository
import com.codebxlk.compose.translator.data.repository.RepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface InterfaceModule {

    @Binds
    fun bindsRepository(repositoryImpl: RepositoryImpl): Repository
}
