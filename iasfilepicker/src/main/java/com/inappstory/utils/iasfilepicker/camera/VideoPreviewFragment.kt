package com.inappstory.utils.iasfilepicker.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inappstory.utils.iasfilepicker.R
import com.inappstory.utils.iasfilepicker.utils.Sizes
import com.inappstory.utils.iasfilepicker.utils.VideoPlayer

class VideoPreviewFragment : PreviewFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        filePath = arguments?.getString("filePath") ?: ""
        return inflater.inflate(R.layout.cs_video_preview_fragment, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<VideoPlayer>(R.id.video_preview).also {
            val scrSizes = Sizes.getScreenSize(it.context)
            val x = scrSizes.x.coerceAtMost(
                9 * scrSizes.y / 16
            )
            val y = scrSizes.y.coerceAtMost(
                16 * scrSizes.x / 9
            )
            it.layoutParams.width = x
            it.layoutParams.height = y
            it.requestLayout()
            it.loadVideo(filePath)
        }
    }
}