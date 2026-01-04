// app/src/main/java/com/colin/map2gpx/gpx/GpxParser.kt
package com.colin.map2gpx.gpx

import android.content.Context
import android.util.Xml
import com.colin.map2gpx.model.Waypoint
import com.colin.map2gpx.util.IconResolver
import com.colin.map2gpx.util.Checkpoint
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

/**
 * GPX parser: extracts <wpt> latitude/longitude and optional <name>/<type>/<sym>.
 * Safe and explicit for Colin's workflow.
 */
object GpxParser {

    // Overload: parse from InputStream (e.g., SAF or file picker)
    fun parse(input: InputStream): List<Waypoint> {
        return try {
            Checkpoint.start("GpxParser", "parse(InputStream)")
            val gpxData = input.bufferedReader().use { it.readText() }
            val result = parse(gpxData)
            Checkpoint.done("GpxParser", "parse(InputStream success: ${result.size} waypoints)")
            result
        } catch (e: IOException) {
            Checkpoint.done("GpxParser", "parse(InputStream failed: ${e.message})")
            emptyList()
        }
    }

    // Existing: parse from String content
    fun parse(gpx: String): List<Waypoint> {
        Checkpoint.start("GpxParser", "parse(String)")
        val waypoints = mutableListOf<Waypoint>()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(gpx.reader())

        var event = parser.eventType
        var currentTag: String? = null

        // Temp holders for a waypoint
        var lat: Double? = null
        var lon: Double? = null
        var name: String? = null
        var type: String? = null
        var sym: String? = null
        var insideWpt = false

        try {
            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        if (currentTag == "wpt") {
                            insideWpt = true
                            lat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull()
                            lon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull()
                            name = null
                            type = null
                            sym = null
                        }
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim()
                        if (insideWpt && !text.isNullOrEmpty()) {
                            when (currentTag) {
                                "name" -> name = text
                                "type" -> type = text
                                "sym"  -> sym = text
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val tagName = parser.name
                        if (tagName == "wpt") {
                            val finalLat = lat
                            val finalLon = lon
                            if (finalLat != null && finalLon != null) {
                                val rawIconType = type ?: sym
                                val iconKey = IconResolver.resolveIconFor(rawIconType)

                                val idKey = "${name ?: "wp"}-${finalLat}-${finalLon}"

                                waypoints.add(
                                    Waypoint(
                                        id = idKey,
                                        lat = finalLat,
                                        lon = finalLon,
                                        name = name,
                                        icon = iconKey
                                    )
                                )
                            }
                            // reset
                            insideWpt = false
                            lat = null
                            lon = null
                            name = null
                            type = null
                            sym = null
                        }
                        currentTag = null
                    }
                }
                event = parser.next()
            }
        } catch (e: XmlPullParserException) {
            Checkpoint.done("GpxParser", "parse(String failed: ${e.message})")
            return emptyList()
        }

        Checkpoint.done("GpxParser", "parse(String success: ${waypoints.size} waypoints)")
        return waypoints
    }

    fun parseGpxFile(context: Context, assetFileName: String): List<Waypoint> {
        return try {
            Checkpoint.start("GpxParser", "parseGpxFile $assetFileName")
            val gpxData = context.assets.open(assetFileName).bufferedReader().use { it.readText() }
            val result = parse(gpxData)
            Checkpoint.done("GpxParser", "parseGpxFile success (${result.size} waypoints)")
            result
        } catch (e: IOException) {
            Checkpoint.done("GpxParser", "parseGpxFile failed: ${e.message}")
            emptyList()
        }
    }
}