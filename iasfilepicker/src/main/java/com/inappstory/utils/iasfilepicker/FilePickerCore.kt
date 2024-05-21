package com.inappstory.utils.iasfilepicker

import com.inappstory.sdk.InAppStoryManager
import com.inappstory.sdk.UseManagerInstanceCallback
import com.inappstory.sdk.modulesconnector.utils.ModuleInitializer

class FilePickerCore : ModuleInitializer {
    override fun initialize() {
        InAppStoryManager.useInstance(object : UseManagerInstanceCallback() {
            @Throws(Exception::class)
            override fun use(manager: InAppStoryManager) {
                manager.filePicker = FilePicker()
            }
        })
    }
}