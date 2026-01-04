package com.colin.map2gpx.ui

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun MapScreen() {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx.applicationContext).apply {
                getMapAsync { map ->
                    map.setStyle("https://demotiles.maplibre.org/style.json") { style ->
                        Log.d("MapScreen", "Style loaded successfully")
                        // Safe place to call MapRenderer.renderWaypoints(context, this, waypoints)
                    }
                }
            }
        }
    )
}