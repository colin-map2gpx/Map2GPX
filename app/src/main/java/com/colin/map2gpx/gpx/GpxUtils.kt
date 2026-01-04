package com.colin.map2gpx.gpx

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

object GpxUtils {

    fun saveGpxToCache(context: Context, gpxXml: String, baseName: String = "route"): File {
        val safeName = baseName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val file = File(context.cacheDir, "$safeName.gpx")

        FileOutputStream(file).use { fos ->
            fos.write(gpxXml.toByteArray(StandardCharsets.UTF_8))
            fos.flush()
        }
        return file
    }

    fun uriForShare(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    // New helpers for your Compose launchers
    fun gpxToBytes(gpxXml: String): ByteArray {
        return gpxXml.toByteArray(StandardCharsets.UTF_8)
    }

    fun defaultGpxFileName(): String {
        return "route.gpx"
    }
}