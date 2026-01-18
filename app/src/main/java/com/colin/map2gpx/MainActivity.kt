package com.colin.map2gpx

import android.os.Bundle
import android.util.Log
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
import org.maplibre.android.maps.MapView
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

                // Create a MapView instance
                val mapView = MapView(this)

                // Render RouteScreen (aligned with new DebugPanel signature)
                RouteScreen(
                    context = this,
                    mapView = mapView,
                    waypoints = waypoints,
                    onDataLoaded = { newWaypoints, trackPoints ->
                        waypoints = newWaypoints
                        Log.d(
                            "MainActivity",
                            "Waypoints updated: ${waypoints.size}, Track points: ${trackPoints.size}"
                        )
                    }
                )
            }
        }
    }
}