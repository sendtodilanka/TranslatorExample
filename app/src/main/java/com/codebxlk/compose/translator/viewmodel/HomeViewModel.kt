package com.codebxlk.compose.translator.viewmodel

import androidx.lifecycle.ViewModel
import com.codebxlk.compose.translator.data.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

}