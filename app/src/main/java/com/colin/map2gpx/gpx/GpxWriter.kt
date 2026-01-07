package com.colin.map2gpx.gpx

import com.colin.map2gpx.model.LatLng
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context
import android.net.Uri
import java.io.OutputStream
import java.io.File
import androidx.core.content.FileProvider

/**
 * GpxWriter
 *
 * Responsible for generating GPX XML strings from a list of LatLng points.
 * Always uses your own LatLng data class with lat/lng properties.
 */
object GpxWriter {

    /**
     * Generate a GPX string for a multi-point route.
     *
     * @param points List of LatLng points (lat/lng)
     * @return GPX XML string
     */
    fun generateMultiPointGpx(points: List<LatLng>): String {
        val sb = StringBuilder()

        // GPX header
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>""").append("\n")
        sb.append("""<gpx version="1.1" creator="Map2GPX" xmlns="http://www.topografix.com/GPX/1/1">""").append("\n")

        // Metadata
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val timestamp = dateFormat.format(Date())
        sb.append("  <metadata>\n")
        sb.append("    <time>$timestamp</time>\n")
        sb.append("  </metadata>\n")

        // Track
        sb.append("  <trk>\n")
        sb.append("    <name>Generated Route</name>\n")
        sb.append("    <trkseg>\n")

        // Each point
        for (point in points) {
            sb.append("      <trkpt lat=\"${point.lat}\" lon=\"${point.lng}\">\n")
            sb.append("        <time>$timestamp</time>\n")
            sb.append("      </trkpt>\n")
        }

        // Close tags
        sb.append("    </trkseg>\n")
        sb.append("  </trk>\n")
        sb.append("</gpx>\n")

        return sb.toString()
    }

    /**
     * Save a GPX string to a given Uri.
     *
     * @param context Android context
     * @param uri Destination Uri chosen by user
     * @param gpxString GPX XML string to write
     */
    fun saveToUri(context: Context, uri: Uri, gpxString: String) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
            outputStream.write(gpxString.toByteArray())
        }
    }

    /**
     * Save GPX to app cache and return a sharable Uri via FileProvider.
     *
     * @param context Android context
     * @param points List of LatLng points
     * @return Uri for the saved GPX file, or null if failed
     */
    fun saveToCacheAndGetUri(context: Context, points: List<LatLng>): Uri? {
        val gpxString = generateMultiPointGpx(points)
        val fileName = "map2gpx_export_${System.currentTimeMillis()}.gpx"
        val file = File(context.cacheDir, fileName)

        return try {
            file.writeText(gpxString)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider", // ensure FileProvider is declared in manifest
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}