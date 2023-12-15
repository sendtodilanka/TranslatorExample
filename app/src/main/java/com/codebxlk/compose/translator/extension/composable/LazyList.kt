package com.codebxlk.compose.translator.extension.composable

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.paging.compose.LazyPagingItems

@Composable
fun <T : Any> LazyPagingItems<T>.rememberLazyGridState(): LazyListState {
    val state = androidx.compose.foundation.lazy.rememberLazyListState()

    return when (itemCount) {
        0 -> remember(this) {
            LazyListState(
                firstVisibleItemIndex = state.firstVisibleItemIndex,
                firstVisibleItemScrollOffset = state.firstVisibleItemScrollOffset
            )
        }
        else -> state
    }
}