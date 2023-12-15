package com.codebxlk.compose.translator.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

private fun <T> Flow<T>.stateIn(scope: CoroutineScope, initialValue: T): StateFlow<T> {
    return this.stateIn(
        scope = scope, started = SharingStarted.Lazily, initialValue = initialValue
    )
}

private fun <T> Flow<T>.shareIn(scope: CoroutineScope): SharedFlow<T> {
    return shareIn(
        scope = scope, started = SharingStarted.Lazily, replay = 1
    )
}