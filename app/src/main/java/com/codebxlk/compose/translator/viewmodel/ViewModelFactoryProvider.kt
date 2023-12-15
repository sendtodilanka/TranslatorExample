package com.codebxlk.compose.translator.viewmodel

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ViewModelFactoryProvider {
    fun languageViewModelFactory(): LanguageViewModel.Factory
}