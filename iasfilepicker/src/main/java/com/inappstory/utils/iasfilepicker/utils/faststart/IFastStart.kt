package com.inappstory.utils.iasfilepicker.utils.faststart

import java.io.IOException

interface IFastStart {
    @kotlin.jvm.Throws(IOException::class)
    fun fastStart() : Boolean

}