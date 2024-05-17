package com.inappstory.utils.iasfilepicker.utils

import android.widget.ImageView

class QueuedTask internal constructor(var imageView: ImageView) {
    var priority = 0
    var started = false
}