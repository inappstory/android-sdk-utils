package com.inappstory.utils.iasfilepicker

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.inappstory.iasutilsconnector.UtilModulesHolder
import com.inappstory.iasutilsconnector.filepicker.IFilePicker
import com.inappstory.iasutilsconnector.filepicker.OnFilesChooseCallback
import com.inappstory.utils.iasfilepicker.file.FilePickerConfig
import com.inappstory.utils.iasfilepicker.file.FilePickerSettings
import com.inappstory.utils.iasfilepicker.utils.BackPressedFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList

class FilePicker : IFilePicker {


    override fun onBackPressed(): Boolean {
        val fm = FilePickerVM.parentFragmentManager ?: return false
        val containerId = FilePickerVM.containerId ?: R.id.fragments_layout
        val parentFragment = fm.findFragmentById(containerId) ?: return false
        val childFragment =
            parentFragment.childFragmentManager.findFragmentById(R.id.fragments_layout)
        if (childFragment !is BackPressedFragment || !childFragment.onBackPressed()) {
            if (parentFragment.childFragmentManager.backStackEntryCount > 1)
                parentFragment.childFragmentManager.popBackStack()
            else
                close()
        }
        return true
    }

    override fun permissionResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        val fm = FilePickerVM.parentFragmentManager ?: return
        val containerId = FilePickerVM.containerId ?: R.id.fragments_layout
        val filePickerFragment = fm.findFragmentById(containerId) ?: return
        if (filePickerFragment is FilePickerMainFragment) {
            filePickerFragment.requestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        }
    }

    override fun setPickerSettings(settings: String?) {
        val filePickerSettings: FilePickerSettings? =
            UtilModulesHolder.jsonParser.fromJson(
                settings, FilePickerSettings::class.java
            )
        FilePickerVM.filePickerSettings = filePickerSettings
    }


    override fun show(
        context: Context?,
        fragmentManager: FragmentManager?,
        containerId: Int,
        callback: OnFilesChooseCallback?
    ) {
        val args = convertSettings()
        FilePickerVM.parentFragmentManager = fragmentManager;
        FilePickerVM.containerId = containerId;
        FilePickerVM.filesChooseCallback = callback
        if (args == null) {
            callback?.onError(
                FilePickerVM.filePickerSettings?.cb,
                FilePickerVM.filePickerSettings?.id,
                "Wrong accept types"
            )
            return
        }
        val coroutine = CoroutineScope(Dispatchers.Main)
        coroutine.launch(Dispatchers.Main) {
            FilePickerMainFragment().apply {
                arguments = args
                try {
                    fragmentManager?.let {
                        val t = it.beginTransaction()
                            .add(containerId, this, "FilePicker")
                        t.addToBackStack(null)
                        t.commitAllowingStateLoss()
                    }

                } catch (e: IllegalStateException) {
                    callback?.onError(
                        FilePickerVM.filePickerSettings?.cb,
                        FilePickerVM.filePickerSettings?.id,
                        e.message.orEmpty()
                    )
                    FilePickerVM.filesChooseCallback = null
                }
            }
        }

    }

    override fun close() {
        FilePickerVM.cancel()
    }

    private fun convertSettings(): Bundle? {
        val settings = FilePickerVM.filePickerSettings
        val acceptTypes = settings?.accept?.split(",") as ArrayList<String>?
        var hasVideo = false
        var hasPhoto = false
        acceptTypes?.forEach {
            if (it.startsWith("image")) hasPhoto = true
            if (it.startsWith("video")) hasVideo = true
        }
        if (!(hasVideo || hasPhoto)) return null;
        val config = settings?.config ?: FilePickerConfig()
        val messages = config.messages ?: hashMapOf()
        return Bundle().apply {
            putBoolean("hasVideo", hasVideo)
            putBoolean("hasPhoto", hasPhoto)
            putStringArrayList(
                "acceptTypes",
                settings?.accept?.split(",") as ArrayList<String>?
            )
            putInt(
                "contentType",
                when {
                    hasVideo && hasPhoto -> 0 //Mix
                    hasVideo -> 2 //Video
                    else -> 1 //Photo
                }
            )
            val (keys, values) = messages.toList().unzip()
            putStringArray(
                "messageNames",
                keys.toTypedArray()
            )
            putStringArray(
                "messages",
                values.toTypedArray()
            )

            putBoolean(
                "allowMultiple",
                settings?.multiple ?: false
            )
            putInt(
                "filePickerFilesLimit",
                settings?.config?.filePickerFilesLimit ?: 10
            )
            putLong(
                "filePickerImageMaxSizeInBytes",
                settings?.config?.filePickerImageMaxSizeInBytes ?: 30000000L
            )
            putLong(
                "filePickerVideoMaxSizeInBytes",
                settings?.config?.filePickerVideoMaxSizeInBytes ?: 30000000L
            )
            putLong(
                "filePickerVideoMaxLengthInSeconds",
                settings?.config?.filePickerVideoMaxLengthInSeconds ?: 30L
            )

        }

    }


}