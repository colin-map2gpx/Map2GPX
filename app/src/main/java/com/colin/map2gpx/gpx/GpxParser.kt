package com.colin.map2gpx.gpx

import android.content.Context
import android.util.Xml
import com.colin.map2gpx.model.Waypoint
import com.colin.map2gpx.util.IconResolver
import com.colin.map2gpx.util.Checkpoint
import org.maplibre.android.geometry.LatLng
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.io.InputStreamReader

/**
 * GPX parser: extracts <wpt> latitude/longitude and optional <name>/<type>/<sym>,
 * plus <trkpt> (and <rtept>) points for route line rendering.
 * Streaming, safe, and explicit for Colin's workflow.
 */
object GpxParser {

    /**
     * Parse from InputStream (preferred: streaming, no full-file read).
     */
    fun parse(input: InputStream): Pair<List<Waypoint>, List<LatLng>> {
        return try {
            Checkpoint.start("GpxParser", "parse(InputStream)")
            val result = parseInternal(InputStreamReader(input))
            Checkpoint.done(
                "GpxParser",
                "parse(InputStream success: ${result.first.size} waypoints, ${result.second.size} track points)"
            )
            result
        } catch (e: Exception) {
            Checkpoint.done("GpxParser", "parse(InputStream failed: ${e.message})")
            Pair(emptyList(), emptyList())
        }
    }

    /**
     * Parse from String content (kept for compatibility).
     */
    fun parse(gpx: String): Pair<List<Waypoint>, List<LatLng>> {
        return try {
            Checkpoint.start("GpxParser", "parse(String)")
            val result = parseInternal(gpx.reader())
            Checkpoint.done(
                "GpxParser",
                "parse(String success: ${result.first.size} waypoints, ${result.second.size} track points)"
            )
            result
        } catch (e: Exception) {
            Checkpoint.done("GpxParser", "parse(String failed: ${e.message})")
            Pair(emptyList(), emptyList())
        }
    }

    /**
     * Parse GPX from assets by filename (streaming).
     */
    fun parseGpxFile(context: Context, assetFileName: String): Pair<List<Waypoint>, List<LatLng>> {
        return try {
            Checkpoint.start("GpxParser", "parseGpxFile $assetFileName")
            context.assets.open(assetFileName).use { input ->
                val result = parseInternal(InputStreamReader(input))
                Checkpoint.done(
                    "GpxParser",
                    "parseGpxFile success (${result.first.size} waypoints, ${result.second.size} track points)"
                )
                result
            }
        } catch (e: Exception) {
            Checkpoint.done("GpxParser", "parseGpxFile failed: ${e.message}")
            Pair(emptyList(), emptyList())
        }
    }

    // ---- Internal streaming parser ----

    private fun parseInternal(reader: java.io.Reader): Pair<List<Waypoint>, List<LatLng>> {
        val waypoints = mutableListOf<Waypoint>()
        val trackPoints = mutableListOf<LatLng>()

        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(reader)

        var event = parser.eventType
        var currentTag: String? = null

        // Waypoint temp state
        var insideWpt = false
        var wptLat: Double? = null
        var wptLon: Double? = null
        var wptName: String? = null
        var wptType: String? = null
        var wptSym: String? = null

        try {
            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        when (currentTag) {
                            // Waypoint start
                            "wpt" -> {
                                insideWpt = true
                                wptLat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull()
                                wptLon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull()
                                wptName = null
                                wptType = null
                                wptSym = null
                            }

                            // Track point
                            "trkpt" -> {
                                val tLat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull()
                                val tLon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull()
                                if (tLat != null && tLon != null) {
                                    trackPoints.add(LatLng(tLat, tLon))
                                }
                            }

                            // Route point (optional support)
                            "rtept" -> {
                                val rLat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull()
                                val rLon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull()
                                if (rLat != null && rLon != null) {
                                    trackPoints.add(LatLng(rLat, rLon))
                                }
                            }

                            // Name/type/sym handled in TEXT
                            "name", "type", "sym" -> { /* handled in TEXT */ }
                        }
                    }

                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim()
                        if (!text.isNullOrEmpty()) {
                            when (currentTag) {
                                "name" -> if (insideWpt) wptName = text
                                "type" -> if (insideWpt) wptType = text
                                "sym"  -> if (insideWpt) wptSym = text
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        val tagName = parser.name
                        if (tagName == "wpt") {
                            val finalLat = wptLat
                            val finalLon = wptLon
                            if (finalLat != null && finalLon != null) {
                                val rawIconType = wptType ?: wptSym
                                val iconKey = IconResolver.resolveIconFor(rawIconType)
                                val safeName = wptName ?: ""
                                val idKey = "${safeName.ifEmpty { "wp" }}-${"%.6f".format(finalLat)}-${"%.6f".format(finalLon)}"

                                waypoints.add(
                                    Waypoint(
                                        id = idKey,
                                        lat = finalLat,
                                        lon = finalLon,
                                        name = safeName,
                                        icon = iconKey
                                    )
                                )
                            }
                            // Reset waypoint state
                            insideWpt = false
                            wptLat = null
                            wptLon = null
                            wptName = null
                            wptType = null
                            wptSym = null
                        }
                        currentTag = null
                    }
                }
                event = parser.next()
            }
        } catch (e: Exception) {
            // Bubble up to caller for checkpoint logging
            throw e
        }

        return Pair(waypoints, trackPoints)
    }
}