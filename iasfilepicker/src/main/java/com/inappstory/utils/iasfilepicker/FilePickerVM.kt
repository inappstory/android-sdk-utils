package com.inappstory.utils.iasfilepicker

import androidx.fragment.app.FragmentManager
import com.inappstory.iasutilsconnector.filepicker.OnFilesChooseCallback
import com.inappstory.utils.iasfilepicker.file.FilePickerSettings

object FilePickerVM {
    var filesChooseCallback: OnFilesChooseCallback? = null
    var parentFragmentManager: FragmentManager? = null
    var containerId: Int? = null
    var filePickerSettings: FilePickerSettings? = null

    fun close() {
        parentFragmentManager?.popBackStack()
    }

    fun cancel() {
        parentFragmentManager?.popBackStack()
        filesChooseCallback?.onCancel(
            filePickerSettings?.cb,
            filePickerSettings?.id
        )
        filesChooseCallback = null
    }

    fun closeWithError(error: String) {
        parentFragmentManager?.popBackStack()
        filesChooseCallback?.onError(
            filePickerSettings?.cb,
            filePickerSettings?.id,
            error
        )
        filesChooseCallback = null
    }

    fun closeWithResult(filesWithTypes: Array<String?>) {
        parentFragmentManager?.popBackStack()
        filesChooseCallback?.onChoose(
            filePickerSettings?.cb,
            filePickerSettings?.id,
            filesWithTypes
        )
        filesChooseCallback = null
    }
}
