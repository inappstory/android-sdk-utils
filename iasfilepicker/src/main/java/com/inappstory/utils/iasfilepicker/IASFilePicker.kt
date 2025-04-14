package com.inappstory.utils.iasfilepicker

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Pair
import androidx.core.database.getLongOrNull
import androidx.fragment.app.FragmentManager
import com.inappstory.iasutilsconnector.UtilModulesHolder
import com.inappstory.iasutilsconnector.filepicker.IFilePicker
import com.inappstory.iasutilsconnector.filepicker.OnFilesChooseCallback
import com.inappstory.utils.iasfilepicker.file.FilePickerAPI
import com.inappstory.utils.iasfilepicker.file.FilePickerSettings
import java.io.File

object IASFilePicker {
    fun getLibraryVersion(): Pair<String, Int> {
        return Pair(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }

    fun customFilePicker(filePicker: CustomFilePicker) {
        UtilModulesHolder.filePicker = object : IFilePicker {
            var localSettings: FilePickerSettings? = null

            override fun filePickerParentShown(context: Context?) {
                filePicker.filePickerParentShown(context)
            }

            override fun close() {
                filePicker.close()
            }

            override fun onBackPressed(): Boolean {
                return filePicker.onBackPressed()
            }

            override fun permissionResult(
                requestCode: Int,
                permissions: Array<String?>,
                grantResults: IntArray
            ) {
                filePicker.permissionResult(requestCode, permissions, grantResults)
            }

            override fun setPickerSettings(settings: String?) {
                val filePickerSettings: FilePickerSettings? =
                    UtilModulesHolder.jsonParser.fromJson(
                        settings,
                        FilePickerSettings::class.java
                    )
                localSettings = filePickerSettings
                filePicker.setPickerSettings(filePickerSettings)
            }

            override fun show(
                context: Context?,
                fragmentManager: FragmentManager?,
                containerId: Int,
                callback: OnFilesChooseCallback?
            ) {
                filePicker.show(
                    context = context,
                    fragmentManager = fragmentManager,
                    containerId = containerId,
                    callback = object : FileChooseCallback {
                        override fun onChoose(
                            cbName: String?,
                            cbId: String?,
                            filesWithTypes: Array<Uri?>?
                        ) {
                            if (context == null) return
                            val urls = arrayListOf<String?>()
                            val fileUrls = arrayListOf<String>()
                            filesWithTypes?.forEach {
                                it?.let { uri ->
                                    val imageProjection = arrayOf(
                                        MediaStore.MediaColumns.TITLE,
                                        MediaStore.MediaColumns.DATA
                                    )
                                    val mergeCursor = context.contentResolver.query(
                                        uri,
                                        imageProjection,
                                        null,
                                        null,
                                        null
                                    ) ?: return

                                    val columnIndexData: Int =
                                        mergeCursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                                    while (mergeCursor.moveToNext()) {
                                        if (columnIndexData == -1) continue
                                        fileUrls.add(mergeCursor.getString(columnIndexData))
                                    }
                                    urls.addAll(fileUrls.map { file ->
                                        Uri.fromFile(File(file)).toString()
                                            .replace("file://", "http://file-assets")
                                    })
                                    mergeCursor.close()
                                }

                            }

                            callback?.onChoose(cbName, cbId, urls.toTypedArray())
                        }

                        override fun onCancel(cbName: String?, cbId: String?) {
                            callback?.onCancel(cbName, cbId)
                        }

                        override fun onError(cbName: String?, cbId: String?, reason: String?) {
                            callback?.onError(cbName, cbId, reason)
                        }
                    })
            }
        }
    }
}