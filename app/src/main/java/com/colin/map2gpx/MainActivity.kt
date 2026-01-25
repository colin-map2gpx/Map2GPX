package com.colin.map2gpx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.colin.map2gpx.model.Waypoint
import com.colin.map2gpx.ui.RouteScreen
import com.colin.map2gpx.ui.theme.Map2GpxTheme
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.geometry.LatLng

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize MapLibre
        MapLibre.getInstance(
            applicationContext,
            "dummy-key",
            WellKnownTileServer.MapLibre
        )

        setContent {
            Map2GpxTheme {
                var waypoints by remember { mutableStateOf(emptyList<Waypoint>()) }
                var trackPoints by remember { mutableStateOf(emptyList<LatLng>()) }

                // Render RouteScreen with current state
                RouteScreen(
                    context = this,
                    waypoints = waypoints,
                    trackPoints = trackPoints,
                    onDataLoaded = { newWaypoints, newTrackPoints ->
                        waypoints = newWaypoints
                        trackPoints = newTrackPoints
                    }
                )
            }
        }
    }
}