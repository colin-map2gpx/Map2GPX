// app/src/main/java/com/colin/map2gpx/model/Waypoint.kt
package com.colin.map2gpx.model

/**
 * Shared waypoint model used across parser, renderer, and share/export.
 * Pure Kotlin data class with no Android dependencies.
 */
data class Waypoint(
    val id: String,             // stable key (e.g., name+lat+lon)
    val lat: Double,
    val lon: Double,
    val name: String? = null,
    val icon: String            // resolved icon key (fuel, tent, etc.)
)