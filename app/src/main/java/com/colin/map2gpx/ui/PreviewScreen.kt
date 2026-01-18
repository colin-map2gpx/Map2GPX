package com.colin.map2gpx.ui

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.colin.map2gpx.share.writeGpxToCache

@Composable
fun PreviewScreen(
    gpxContent: String = "<gpx><trk><name>Sample Route</name></trk></gpx>"
) {
    val context: Context = LocalContext.current

    // Generate a Uri once per gpxContent
    val gpxUri: Uri? = remember(gpxContent) {
        writeGpxToCache(context, "route.gpx", gpxContent)
    }

    Column {
        Text("Preview stub")

        ShareGpxButton(
            context = context,
            gpxUri = gpxUri,
            label = "Share GPX"
        )
    }
}