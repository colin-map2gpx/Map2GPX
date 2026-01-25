package com.colin.map2gpx.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.colin.map2gpx.map.MapRenderer
import com.colin.map2gpx.model.Waypoint
import org.maplibre.android.maps.MapView
import org.maplibre.android.geometry.LatLng
import org.maplibre.geojson.Point

@Composable
fun MapScreen(
    context: Context,
    waypoints: List<Waypoint>,
    trackPoints: List<LatLng>,
    modifier: Modifier = Modifier
) {
    var mapView: MapView? = null

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).also { createdView ->
                    mapView = createdView
                    // Initialize base style
                    MapRenderer.initMap(context, createdView)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // When you have data, convert LatLng -> Point before rendering
        mapView?.let { safeMapView ->
            val trackPointsAsPoints = trackPoints.map { latLng ->
                Point.fromLngLat(latLng.longitude, latLng.latitude)
            }
            MapRenderer.renderAll(context, safeMapView, waypoints, trackPointsAsPoints)
        }
    }
}