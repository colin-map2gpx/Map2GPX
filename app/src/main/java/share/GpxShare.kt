package com.colin.map2gpx.share

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun writeGpxToCache(
    context: Context,
    fileName: String = "route.gpx",
    gpxContent: String
): Uri? {
    val dir = File(context.cacheDir, "gpx").apply { mkdirs() }
    val file = File(dir, fileName)
    file.writeText(gpxContent)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}