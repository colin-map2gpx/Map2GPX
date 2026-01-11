package com.colin.map2gpx.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.colin.map2gpx.model.Waypoint
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object RouteExtractor {

    private const val TAG = "RouteExtractor"

    /**
     * Expand a short maps.app.goo.gl link into the final Google Maps URL.
     * Uses WebViewResolver to handle JavaScript/meta refresh redirects.
     *
     * This is async—provide a callback to receive the final URL (or null on failure).
     */
    fun expandShortUrlAsync(
        context: Context,
        shortUrl: String,
        onResult: (String?) -> Unit
    ) {
        if (!shortUrl.contains("maps.app.goo.gl")) {
            Log.d(TAG, "Short URL not detected, returning as-is: $shortUrl")
            onResult(shortUrl)
            return
        }

        WebViewResolver.resolve(context, shortUrl, timeoutMs = 8000) { finalUrl ->
            if (finalUrl.isNullOrBlank()) {
                Log.e(TAG, "Failed to expand short URL: $shortUrl")
                onResult(null)
            } else {
                Log.d(TAG, "Expanded short URL (WebView): $finalUrl")
                onResult(finalUrl)
            }
        }
    }

    /**
     * Parse waypoints from a full Google Maps URL.
     * Supports both /dir/... path style and ?api=1&origin=...&destination=... query style.
     *
     * Returns a list of Waypoint objects with id + name populated.
     * Lat/lon/icon are stubbed with defaults and can be enriched later.
     */
    fun parseWaypoints(expandedUrl: String?): List<Waypoint> {
        if (expandedUrl.isNullOrBlank()) {
            Log.e(TAG, "parseWaypoints called with null/blank URL")
            return emptyList()
        }

        return try {
            val uri = Uri.parse(expandedUrl)
            val rawPoints = mutableListOf<String>()

            if (expandedUrl.contains("/dir/")) {
                val parts = expandedUrl.split("/dir/")
                if (parts.size > 1) {
                    val segment = parts[1]
                    rawPoints.addAll(
                        segment.split("/")
                            .filter { it.isNotBlank() }
                            .map { decode(it) }
                    )
                }
            } else if (uri.getQueryParameter("api") == "1") {
                uri.getQueryParameter("origin")?.let { rawPoints.add(decode(it)) }
                uri.getQueryParameter("destination")?.let { rawPoints.add(decode(it)) }
                uri.getQueryParameter("waypoints")?.let {
                    rawPoints.addAll(it.split("|").map { wp -> decode(wp) })
                }
            }

            val waypoints = rawPoints.mapIndexed { idx, name ->
                Waypoint(
                    id = idx.toString(),
                    name = name.trim(),
                    lat = 0.0,          // placeholder until enriched
                    lon = 0.0,          // placeholder until enriched
                    icon = "default"    // placeholder until enriched
                )
            }

            Log.d(TAG, "Parsed waypoints: $waypoints")
            waypoints
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse waypoints: ${e.message}", e)
            emptyList()
        }
    }

    private fun decode(value: String): String {
        return try {
            URLDecoder.decode(value, StandardCharsets.UTF_8.toString()).trim()
        } catch (e: Exception) {
            value.replace("+", " ").trim()
        }
    }
}