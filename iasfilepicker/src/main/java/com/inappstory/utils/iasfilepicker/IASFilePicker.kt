package com.inappstory.utils.iasfilepicker

import android.util.Pair
import com.inappstory.sdk.BuildConfig

object IASFilePicker {
    fun getLibraryVersion(): Pair<String, Int> {
        return Pair(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }
}