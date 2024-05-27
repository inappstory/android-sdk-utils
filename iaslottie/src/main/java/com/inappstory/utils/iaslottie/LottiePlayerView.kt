package com.inappstory.utils.iaslottie

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.content.Context
import android.util.AttributeSet
import android.util.Pair
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
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
        this.scaleType = ScaleType.FIT_CENTER

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
                        if (
                            arrayOf("lottie", "zip").contains(
                                (source.second as File).extension
                            )
                        ) {
                            val stream = FileInputStream((source.second as File))
                            val manifest = LottieUtils().getManifestJson(source.second as File)
                            val result = LottieCompositionFactory.fromZipStreamSync(
                                ZipInputStream(stream),
                                source.first as String
                            )
                            val composition = result.value ?: return
                            totalFrame = composition.durationFrames
                            setComposition(composition)
                        } else {
                            try {
                                setAnimation(
                                    FileInputStream(source.second as File),
                                    source.first as String
                                )
                            } catch (e: FileNotFoundException) {
                            }
                        }
                    }

                    is ZipInputStream -> {
                        //  this.setImageAssetsFolder();
                        val result = LottieCompositionFactory.fromZipStreamSync(
                            source.second as ZipInputStream,
                            source.first as String
                        )
                        val composition = result.value ?: return
                        setComposition(composition)
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
    }



    override fun play() {
        playAnimation()
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

    override fun isLooped(): Boolean {
        return false
    }

    private var maxFrame: Float = 99f
    private var totalFrame: Float = 99f

    override fun setAnimProgress(progress: Float) {
        val coefficient = maxFrame / totalFrame
        super.setProgress(progress * coefficient)
    }

    override fun setLoop(isLooped: Boolean) {
        if (isLooped) this.repeatCount = LottieDrawable.INFINITE else this.repeatCount = 0
    }
}