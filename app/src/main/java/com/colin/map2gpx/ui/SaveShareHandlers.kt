package com.colin.map2gpx.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * Centralized save/share handlers.
 * Currently includes a Share GPX button stub.
 */
@Composable
fun ShareGpxButton(
    context: Context,
    gpxUri: Uri?,
    label: String = "Share GPX"
) {
    Button(
        onClick = {
            val uri = gpxUri ?: return@Button
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/gpx+xml"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(shareIntent, "Share GPX")
            )
        }
    ) {
        Text(text = label)
    }
}