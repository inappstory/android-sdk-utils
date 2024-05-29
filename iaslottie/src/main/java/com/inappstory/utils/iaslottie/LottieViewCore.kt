package com.inappstory.utils.iaslottie

import android.content.Context
import android.util.Pair
import com.inappstory.iasutilsconnector.ModuleInitializer
import com.inappstory.iasutilsconnector.UtilModulesHolder
import com.inappstory.iasutilsconnector.lottie.ILottieView
import com.inappstory.iasutilsconnector.lottie.ILottieViewGenerator

class LottieViewCore : ModuleInitializer {
    override fun initialize() {
        UtilModulesHolder.lottieViewGenerator = object : ILottieViewGenerator {
            override fun getView(context: Context?): ILottieView {
                return LottiePlayerView(context)
            }

        }
    }

    fun getLibraryVersion(): Pair<String, Int> {
        return Pair(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }
}