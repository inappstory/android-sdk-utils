package com.inappstory.utils.iasfilepicker.file

interface FileClickCallback {
    fun select(file: SelectedFile)

    fun unselect(file: SelectedFile)
}