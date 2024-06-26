package com.inappstory.utils.iasfilepicker

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.inappstory.utils.iasfilepicker.camera.CameraFlowFragment
import com.inappstory.utils.iasfilepicker.file.FilePickerFragment
import com.inappstory.utils.iasfilepicker.file.FilePreviewsCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class FilePickerMainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.cs_file_choose_activity, container, false)
    }

    fun requestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray
    ) {
        val fragment = childFragmentManager.findFragmentByTag("FILE_CHOOSE")
        if (fragment is FilePickerFragment) {
            fragment.requestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null)
            openFilePickerScreen(requireArguments())
    }



    fun openFileCameraScreen(bundle: Bundle) {
        CameraFlowFragment().apply {
            arguments = bundle
            openFragment(this, "CAMERA_FLOW", add = true)
        }
    }

    private fun openFilePickerScreen(bundle: Bundle) {
        FilePickerFragment().apply {
            arguments = bundle
            openFragment(this, "FILE_CHOOSE")
        }
    }

    private fun openFragment(fragment: Fragment, tag: String, add: Boolean = false) {
        try {
            val fragmentManager =
                childFragmentManager
            var t = fragmentManager.beginTransaction()
            t = if (add)
                t.add(R.id.fragments_layout, fragment, tag)
            else
                t.replace(R.id.fragments_layout, fragment, tag)
            t.addToBackStack(null)
            t.commitAllowingStateLoss()
        } catch (e: IllegalStateException) {
            FilePickerVM.filesChooseCallback?.onError(
                FilePickerVM.filePickerSettings?.cb,
                FilePickerVM.filePickerSettings?.id,
                e.message.orEmpty()
            )
            FilePickerVM.filesChooseCallback = null
            FilePickerVM.close()
        }
    }

    fun sendResult(files: Array<String>) {
        FilePickerVM.closeWithResult(files.map {
            Uri.fromFile(File(it)).toString()
                .replace("file://", "http://file-assets")
        }.toTypedArray())
    }


    private val cache = FilePreviewsCache(false)

    fun loadPreview(path: String, imageView: ImageView, isVideo: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            cache.loadPreview(path, imageView, false, isVideo)
        }
    }
}