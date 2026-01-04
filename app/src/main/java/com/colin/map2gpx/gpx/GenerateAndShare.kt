package com.colin.map2gpx.gpx

import android.content.Context
import android.content.Intent

/**
 * Simple helper to generate a dummy GPX string and share it.
 * Later you can replace the hardcoded points with real parsed routes.
 */
fun generateAndShareGpx(context: Context) {
    // Example GPX content
    val gpxString = """
        <?xml version="1.0" encoding="UTF-8"?>
        <gpx version="1.1" creator="Map2GPX">
          <trk>
            <name>Sample Route</name>
            <trkseg>
              <trkpt lat="37.4219983" lon="-122.084"></trkpt>
              <trkpt lat="37.422" lon="-122.085"></trkpt>
            </trkseg>
          </trk>
        </gpx>
    """.trimIndent()

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/gpx+xml"
        putExtra(Intent.EXTRA_SUBJECT, "Generated GPX")
        putExtra(Intent.EXTRA_TEXT, gpxString)
    }
    val chooser = Intent.createChooser(sendIntent, "Share GPX")
    context.startActivity(chooser)
}