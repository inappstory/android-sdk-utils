package com.inappstory.utils.iaslottie

import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class LottieUtils {
    fun getManifestJson(file: File): String? {
        try {
            var hasManifest = false
            val sb = StringBuilder()
            if (file.exists()) {
                val stream = FileInputStream(file)
                val zipStream = ZipInputStream(stream)
                var entry: ZipEntry? = zipStream.nextEntry
                while (entry != null) {
                    if (entry.name.equals("manifest.json", ignoreCase = true)) {
                        var count: Int
                        val buffer = ByteArray(1024)
                        hasManifest = true
                        while (zipStream.read(buffer).also { count = it } != -1) {
                            sb.append(String(buffer, 0, count))
                        }
                        break
                    }
                    entry = zipStream.nextEntry
                }
            } else {
                return null
            }
            if (hasManifest) return sb.toString()
        } catch (_: Exception) {

        }
        return null
    }
}