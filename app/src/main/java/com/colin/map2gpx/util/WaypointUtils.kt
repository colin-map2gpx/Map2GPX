package com.colin.map2gpx.util

/**
 * Delegates to IconResolver so we maintain one mapping table.
 * Existing imports in MapScreen (getWaypointIcon, resolveIconFor) continue to work.
 */
fun getWaypointIcon(type: String): Int {
    return IconResolver.resolveIconDrawable(type)
}

fun resolveIconFor(label: String): String {
    return IconResolver.resolveIconFor(label)
}