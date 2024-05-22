package com.inappstory.utils.iasfilepicker

import android.util.Pair

object IASFilePicker {
    fun getLibraryVersion(): Pair<String, Int> {
        return Pair(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }
}