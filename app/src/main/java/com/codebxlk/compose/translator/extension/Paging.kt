package com.codebxlk.compose.translator.extension

import androidx.paging.PagingData
import androidx.paging.insertSeparators

fun <T : Any> PagingData<T>.addBannerAds(
    totalAdCount: Int,
    isPremium: Boolean,
    bannerItem: (Int) -> T
): PagingData<T> {
    if (isPremium) return this

    var itemCount = 0
    var insertedAdCount = 0

    return insertSeparators { _, _ ->
        if (insertedAdCount < totalAdCount && ++itemCount % 13 == 0) {
            insertedAdCount++
            bannerItem(itemCount)
        } else {
            null
        }
    }
}

fun <T : Any> PagingData<T>.addBannerAds(
    isPremium: Boolean,
    bannerItem: (Int) -> T,
    initItemPos: Int = 5,
    adsInterval: Int = 10
): PagingData<T> {
    if (isPremium) return this

    var tempCount = 0
    return insertSeparators { before, after ->
        tempCount++

        val isInitPos = tempCount == initItemPos
        val isMiddlePos = (tempCount - initItemPos) % adsInterval == 0 && tempCount > initItemPos
        val isEndOfListWithLessItems = before != null && after == null && tempCount < initItemPos

        if (
            isInitPos
            || isMiddlePos
            || isEndOfListWithLessItems
        ) bannerItem(tempCount) else null
    }
}