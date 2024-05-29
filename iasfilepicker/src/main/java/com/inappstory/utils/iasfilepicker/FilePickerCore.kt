package com.inappstory.utils.iasfilepicker

import com.inappstory.iasutilsconnector.ModuleInitializer
import com.inappstory.iasutilsconnector.UtilModulesHolder


class FilePickerCore : ModuleInitializer {
    override fun initialize() {
        UtilModulesHolder.filePicker = FilePicker()
    }
}