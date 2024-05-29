package com.inappstory.utils.iasfilepicker.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.inappstory.utils.iasfilepicker.utils.Sizes
import com.inappstory.utils.iasfilepicker.R

class PhotoPreviewFragment : PreviewFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        filePath = arguments?.getString("filePath") ?: ""
        return inflater.inflate(R.layout.cs_photo_preview_fragment, null)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageView>(R.id.photo_preview).also {
            val scSize = Sizes.getScreenSize(it.context)
            val x = scSize.x.coerceAtMost(
                9 * scSize.y / 16
            )
            val y = scSize.y.coerceAtMost(
                16 * scSize.x / 9
            )
            it.layoutParams.width = x
            it.layoutParams.height = y
            it.requestLayout()
            if (parentFragment is CameraFlowFragment) {
                (parentFragment as CameraFlowFragment).loadPreview(
                    path = filePath,
                    isVideo = false,
                    imageView = it
                )
            }
        }
    }


}