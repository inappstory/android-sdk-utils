package com.inappstory.utils.iaslottie

import android.util.Pair

object IASLottie {
    fun getLibraryVersion(): Pair<String, Int> {
        return Pair(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }
}