package com.colin.map2gpx.ui.route

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.colin.map2gpx.model.Waypoint

@Composable
fun WaypointList(waypoints: List<Waypoint>) {
    Column {
        waypoints.forEachIndexed { index, wp ->
            Text(text = "Waypoint ${index + 1}: ${wp.icon} @ (${wp.lat}, ${wp.lon})")
        }
    }
}