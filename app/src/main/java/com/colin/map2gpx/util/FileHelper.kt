package com.colin.map2gpx.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object FileHelper {

    fun saveGpxToFile(context: Context, gpxContent: String, fileName: String = "route.gpx"): File {
        val file = File(context.cacheDir, fileName)
        file.writeText(gpxContent)
        return file
    }

    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/gpx+xml"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share GPX file"))
    }
}