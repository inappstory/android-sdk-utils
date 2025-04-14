package com.inappstory.utils.iasfilepicker

import android.net.Uri


interface FileChooseCallback {
    fun onChoose(cbName: String?, cbId: String?, filesWithTypes: Array<Uri?>?)

    fun onCancel(cbName: String?, cbId: String?)

    fun onError(cbName: String?, cbId: String?, reason: String?)
}