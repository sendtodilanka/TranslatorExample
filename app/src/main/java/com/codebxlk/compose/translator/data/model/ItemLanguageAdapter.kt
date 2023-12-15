package com.codebxlk.compose.translator.data.model

sealed interface ItemLanguageAdapter {
    data class SectionTitle(val title: String) : ItemLanguageAdapter
    data class Item(val language: Language) : ItemLanguageAdapter
}