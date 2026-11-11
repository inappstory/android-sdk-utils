package com.inappstory.utils.iasfilepicker.file

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FilePreviewsList @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    init {
        layoutManager = GridLayoutManager(
            context, 3,
            GridLayoutManager.VERTICAL,
            false
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    fun load(
        cameraCallback: OpenCameraClickCallback,
        filePickerClickCallback: OpenFilePickerClickCallback
    ) {

        val adapter = FilePreviewsAdapter(
            cameraCallback = cameraCallback,
            filePickerClickCallback = filePickerClickCallback
        )
        adapter.setHasStableIds(true)
        setAdapter(adapter)
    }
}