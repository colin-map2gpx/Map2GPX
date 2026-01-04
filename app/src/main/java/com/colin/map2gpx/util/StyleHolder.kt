package com.colin.map2gpx.util

import org.maplibre.android.maps.Style

object StyleHolder {
    var currentStyle: Style? = null
    const val WAYPOINT_SOURCE_ID = "waypoint-source"
    const val WAYPOINT_LAYER_ID = "waypoint-layer"
}