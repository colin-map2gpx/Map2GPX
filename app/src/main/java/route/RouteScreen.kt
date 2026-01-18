package com.colin.map2gpx.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.colin.map2gpx.model.Waypoint
import com.colin.map2gpx.map.MapRenderer
import org.maplibre.android.maps.MapView
import org.maplibre.android.geometry.LatLng

@Composable
fun RouteScreen(
    context: Context,
    mapView: MapView,
    waypoints: List<Waypoint>,
    onDataLoaded: (List<Waypoint>, List<LatLng>) -> Unit = { _, _ -> }
) {
    // Keep track of current waypoints in Compose state
    var waypointsState by remember { mutableStateOf(waypoints) }

    Column {
        // Host the MapView inside Compose
        AndroidView(factory = { _: Context ->
            mapView
        })

        // Overlay DebugPanel with buttons wired to callbacks
        DebugPanel(
            context = context,
            mapView = mapView,
            currentWaypoints = waypointsState,
            onDataLoaded = { newWaypoints, trackPoints ->
                // Update state or propagate upstream
                waypointsState = newWaypoints
                onDataLoaded(newWaypoints, trackPoints)
                // Render both waypoints and track line
                MapRenderer.renderAll(context, mapView, newWaypoints, trackPoints)
            }
        )
    }
}