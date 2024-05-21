package com.inappstory.utils.iaslottie

import android.util.Pair
import com.inappstory.sdk.BuildConfig
import com.inappstory.sdk.InAppStoryManager
import com.inappstory.sdk.UseManagerInstanceCallback
import com.inappstory.sdk.modulesconnector.utils.ModuleInitializer
import com.inappstory.sdk.modulesconnector.utils.lottie.ILottieViewGenerator

class LottieViewCore : ModuleInitializer {
    override fun initialize() {
        InAppStoryManager.useInstance(object : UseManagerInstanceCallback() {
            @Throws(Exception::class)
            override fun use(manager: InAppStoryManager) {
                manager.lottieViewGenerator =
                    ILottieViewGenerator { context -> LottiePlayerView(context) }
            }
        })
    }

    fun getLibraryVersion(): Pair<String, Int> {
        return Pair(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }
}