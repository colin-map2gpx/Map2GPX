package com.colin.map2gpx.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.maps.MapView

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val mapView = MapView(context.applicationContext)

    Box(modifier = Modifier.fillMaxSize()) {
        // Show the map
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        // Add DebugPanel at the bottom
        DebugPanel(
            context = context,
            mapView = mapView,
            modifier = Modifier.align(Alignment.BottomCenter),
            onWaypointsLoaded = { waypoints ->
                // optional: handle waypoints if needed
            }
        )
    }

    // Initialize map style
    mapView.getMapAsync { map ->
        map.setStyle("https://demotiles.maplibre.org/style.json") {
            Log.d("MapScreen", "Style loaded successfully")
        }
    }
}