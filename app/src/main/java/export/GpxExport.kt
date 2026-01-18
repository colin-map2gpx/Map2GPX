package com.colin.map2gpx.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.colin.map2gpx.model.Waypoint
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

object GpxExport {

    fun exportWaypointsToGpx(
        context: Context,
        waypoints: List<Waypoint>,
        fileName: String = "route_export.gpx"
    ): Uri? {
        return try {
            val gpxContent = buildGpx(waypoints)

            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos).use { writer ->
                    writer.write(gpxContent)
                }
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun buildGpx(waypoints: List<Waypoint>): String {
        val builder = StringBuilder()
        builder.append("""<?xml version="1.0" encoding="UTF-8"?>""").append("\n")
        builder.append("""<gpx version="1.1" creator="Map2GPX">""").append("\n")

        waypoints.forEach { wp ->
            builder.append(
                """  <wpt lat="${wp.lat}" lon="${wp.lon}"><name>${wp.name}</name></wpt>"""
            ).append("\n")
        }

        builder.append("</gpx>")
        return builder.toString()
    }
}