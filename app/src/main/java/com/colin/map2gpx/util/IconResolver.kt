package com.colin.map2gpx.util

import com.colin.map2gpx.R

/**
 * Maps waypoint iconType strings to icon keys used by the map SymbolLayer.
 * Make sure these keys match the drawables you registered in MapScreen.kt.
 */
object IconResolver {

    /**
     * Resolve an iconType string to a SymbolLayer key.
     * Example: "tent" -> "tent"
     */
    fun resolveIconFor(iconType: String?): String {
        val type = iconType?.lowercase() ?: ""

        return when {
            type.contains("camp") || type.contains("tent") -> "tent"
            type.contains("hotel") || type.contains("guest") -> "hotel"
            type.contains("fuel") || type.contains("gas") || type.contains("petrol") -> "fuel"
            type.contains("food") || type.contains("eat") || type.contains("restaurant") -> "food"
            type.contains("repair") || type.contains("service") || type.contains("wrench") -> "wrench"
            type.contains("view") || type.contains("scenic") -> "camera"
            else -> "motorcycle" // default fallback
        }
    }

    /**
     * Resolve an icon key to its drawable resource ID.
     * Example: "tent" -> R.drawable.tent
     */
    fun resolveIconDrawable(iconKey: String): Int {
        return when (iconKey) {
            "tent" -> R.drawable.tent
            "fuel" -> R.drawable.fuel
            "motorcycle" -> R.drawable.motorcycle
            "camera" -> R.drawable.camera
            "wrench" -> R.drawable.wrench
            "food" -> R.drawable.food
            "hotel" -> R.drawable.hotel
            else -> R.drawable.motorcycle
        }
    }
}