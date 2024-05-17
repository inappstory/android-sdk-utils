package com.inappstory.utils.iaslottie

import android.content.Context
import android.util.AttributeSet
import android.util.Pair
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.inappstory.sdk.modulesconnector.utils.lottie.ILottieView
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.zip.ZipInputStream

class LottiePlayerView : LottieAnimationView, ILottieView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun setSource(source: Any) {
        if (source is Pair<*, *>) {
            if (source.first is String) {
                when (source.second) {
                    is String -> {
                        setAnimation(
                            ByteArrayInputStream(
                                (source.second as String).toByteArray()
                            ),
                            source.first as String
                        )
                    }

                    is File -> {
                        try {
                            setAnimation(
                                FileInputStream(source.second as File),
                                source.first as String
                            )
                        } catch (e: FileNotFoundException) {
                        }
                    }

                    is ZipInputStream -> {
                        //  this.setImageAssetsFolder();
                        setAnimation(
                            source.second as ZipInputStream,
                            source.first as String
                        )
                    }
                }
                if (source.second is InputStream) {
                    setAnimation(
                        source.second as InputStream,
                        source.first as String
                    )
                }
            }
        }
        this.scaleType = ScaleType.CENTER_CROP
    }

    override fun play() {
        playAnimation()
        addAnimatorUpdateListener { }
    }

    override fun stop() {
        pauseAnimation()
    }

    override fun pause() {
        pauseAnimation()
    }

    override fun resume() {
        resumeAnimation()
    }

    override fun restart() {
        playAnimation()
    }

    override fun setAnimProgress(progress: Float) {
        super.setProgress(progress)
    }

    override fun setLoop(isLooped: Boolean) {
        if (isLooped) this.repeatCount = LottieDrawable.INFINITE else this.repeatCount = 0
    }
}