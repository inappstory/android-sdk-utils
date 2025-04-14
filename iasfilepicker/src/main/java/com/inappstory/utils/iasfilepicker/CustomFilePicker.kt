package com.inappstory.utils.iasfilepicker

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.inappstory.utils.iasfilepicker.file.FilePickerSettings

interface CustomFilePicker {
    fun setPickerSettings(settings: FilePickerSettings?)

    fun filePickerParentShown(context: Context?)

    fun onBackPressed(): Boolean

    fun permissionResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    )

    fun show(
        context: Context?,
        fragmentManager: FragmentManager?,
        containerId: Int,
        callback: FileChooseCallback?
    )

    fun close()

}