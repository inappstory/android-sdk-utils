package com.inappstory.utils.iasfilepicker.file

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.inappstory.utils.iasfilepicker.R
import com.inappstory.utils.iasfilepicker.utils.Sizes

internal class FilePreviewsAdapter(
    val cameraCallback: OpenCameraClickCallback,
    val filePickerClickCallback: OpenFilePickerClickCallback
) : RecyclerView.Adapter<FilePreviewsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilePreviewsHolder {
        val v = when (viewType) {
            0 -> LayoutInflater.from(parent.context).inflate(
                R.layout.cs_file_camera_cell,
                parent, false
            )
            else -> LayoutInflater.from(parent.context).inflate(
                R.layout.cs_file_picker_cell,
                parent, false
            )
        }
        v.layoutParams.width = Sizes.getScreenSize(v.context).x / 3
        v.layoutParams.height = 16 * v.layoutParams.width / 9
        return FilePreviewsHolder(v)
    }

    override fun onBindViewHolder(holder: FilePreviewsHolder, position: Int) {
        if (position == 0) {
            holder.itemView.setOnClickListener { cameraCallback.open() }
        } else if (position == 1) {
            holder.itemView.setOnClickListener { filePickerClickCallback.open() }
        }
    }

    override fun getItemViewType(position: Int): Int = position

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int = 2
}