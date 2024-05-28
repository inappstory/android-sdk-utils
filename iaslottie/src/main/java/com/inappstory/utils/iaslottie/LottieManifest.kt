package com.inappstory.utils.iaslottie

import com.inappstory.sdk.network.annotations.models.SerializedName

class LottieManifest {
    @SerializedName("animations")
    var lottieAnimations: List<LottieManifestAnimation>? = null
}