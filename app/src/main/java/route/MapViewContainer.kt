package com.colin.map2gpx.ui.route

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.colin.map2gpx.map.MapRenderer   // ✅ correct import
import com.colin.map2gpx.model.Waypoint
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun MapViewContainer(
    waypoints: List<Waypoint>,
    trackPoints: List<LatLng>,   // ✅ use LatLng, not Pair<Double, Double>
    showRoute: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Create and retain a single MapView instance
    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
            onStart()
            onResume()
        }
    }

    // Dispose MapView when Composable leaves
    DisposableEffect(mapView) {
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            view.getMapAsync { map ->
                map.setStyle(
                    Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")
                ) {
                    MapRenderer.renderWaypoints(context, view, waypoints)
                    if (showRoute) {
                        MapRenderer.renderTrack(context, view, trackPoints)
                    }
                }
            }
        }
    )
}