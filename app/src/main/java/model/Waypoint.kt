// app/src/main/java/com/colin/map2gpx/model/Waypoint.kt
package com.colin.map2gpx.model

/**
 * Represents a single waypoint in a route.
 * - id: unique identifier (string index or GPX ID)
 * - name: human-readable label (parsed from Google Maps or GPX, "" if missing)
 * - lat/lon: coordinates (always non-null once parsed from GPX)
 * - icon: resolved icon type (fuel, tent, etc.), always non-null
 */
data class Waypoint(
    val id: String,    // unique identifier
    val name: String,  // display name ("" if missing)
    val lat: Double,   // latitude
    val lon: Double,   // longitude
    val icon: String   // resolved icon key
)