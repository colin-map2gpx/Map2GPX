package com.colin.map2gpx.ui.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.colin.map2gpx.model.Waypoint
import com.colin.map2gpx.map.MapRenderer
import org.maplibre.android.maps.MapView

@Composable
fun MapViewContainer(
    waypoints: List<Waypoint>,
    showRoute: Boolean,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                // Always render waypoints first
                MapRenderer.renderWaypoints(context, this, waypoints)

                // If user confirmed, also render route line
                if (showRoute) {
                    MapRenderer.renderRouteLine(context, this, waypoints)
                }
            }
        }
    )
}