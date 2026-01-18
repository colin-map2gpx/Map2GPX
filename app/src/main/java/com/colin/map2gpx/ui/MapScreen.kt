package com.colin.map2gpx.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.colin.map2gpx.model.Waypoint
import com.colin.map2gpx.map.MapRenderer
import org.maplibre.android.maps.MapView
import org.maplibre.android.geometry.LatLng

@Composable
fun MapScreen(
    mapView: MapView,
    modifier: Modifier = Modifier,
    onDataLoaded: (List<Waypoint>, List<LatLng>) -> Unit = { _, _ -> }
) {
    val context: Context = LocalContext.current

    // Keep track of current waypoints in Compose state
    var waypointsState by remember { mutableStateOf(emptyList<Waypoint>()) }

    // Drive MapView lifecycle inside Compose
    DisposableEffect(Unit) {
        mapView.onStart()
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Render the MapLibre map and ensure a style is loaded
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.getMapAsync { map ->
                    map.setStyle("https://demotiles.maplibre.org/style.json") {
                        Log.d("MapScreen", "Map style loaded")
                        // Sources/layers are added via MapRenderer when data arrives
                    }
                }
            }
        )

        // Overlay the debug panel at the bottom center
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            DebugPanel(
                context = context,
                mapView = mapView,
                currentWaypoints = waypointsState,
                onDataLoaded = { waypoints, trackPoints ->
                    Log.d("MapScreen", "Loaded ${waypoints.size} waypoints + ${trackPoints.size} track points")
                    waypointsState = waypoints
                    onDataLoaded(waypoints, trackPoints)
                    MapRenderer.renderAll(context, mapView, waypoints, trackPoints)
                }
            )
        }
    }
}