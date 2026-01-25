package com.colin.map2gpx.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import com.colin.map2gpx.export.GpxExport
import com.colin.map2gpx.gpx.GpxParser
import com.colin.map2gpx.map.MapRenderer
import com.colin.map2gpx.model.Waypoint
import org.maplibre.android.maps.MapView
import org.maplibre.android.geometry.LatLng
import org.maplibre.geojson.Point

// ✅ Correct imports for icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Share

@Composable
fun DebugPanel(
    context: Context,
    mapView: MapView?,
    modifier: Modifier = Modifier,
    assetPath: String = "sample.gpx",
    currentWaypoints: List<Waypoint> = emptyList(),
    onDataLoaded: (List<Waypoint>, List<LatLng>) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Button 1: Preview sample route
            Button(
                onClick = {
                    Log.d("DebugPanel", "Reading demo asset: $assetPath")
                    val (waypoints, trackPoints) = context.assets.open(assetPath).use { inputStream ->
                        GpxParser.parse(inputStream)
                    }
                    Log.d("DebugPanel", "Parsed ${waypoints.size} waypoints, ${trackPoints.size} track points")

                    mapView?.let { safeMapView ->
                        val trackPointsAsPoints = trackPoints.map { latLng ->
                            Point.fromLngLat(latLng.longitude, latLng.latitude)
                        }
                        MapRenderer.renderAll(context, safeMapView, waypoints, trackPointsAsPoints)
                        Log.d("DebugPanel", "Rendered ${waypoints.size} waypoints + ${trackPoints.size} track points")
                    }

                    onDataLoaded(waypoints, trackPoints)
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Preview")
                Spacer(Modifier.width(4.dp))
                Text("Preview Route", maxLines = 1)
            }

            // Button 2: Load Route (stub for now)
            Button(
                onClick = {
                    Log.d("DebugPanel", "TODO: Handle Load Route from Share flow")
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.FileOpen, contentDescription = "Load")
                Spacer(Modifier.width(4.dp))
                Text("Load Route", maxLines = 1)
            }

            // Button 3: Export GPX
            Button(
                onClick = {
                    if (currentWaypoints.isEmpty()) {
                        Log.w("DebugPanel", "No waypoints to export. Load or preview a route first.")
                    } else {
                        shareGpx(context, currentWaypoints)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Export")
                Spacer(Modifier.width(4.dp))
                Text("Export GPX", maxLines = 1)
            }
        }
    }
}

/**
 * Helper to export current waypoints to cacheDir and launch Android Share Sheet.
 */
private fun shareGpx(context: Context, waypoints: List<Waypoint>) {
    val uri = GpxExport.exportWaypointsToGpx(context, waypoints, "route_export.gpx")
    if (uri != null) {
        val intent = ShareCompat.IntentBuilder(context)
            .setType("application/gpx+xml")
            .setStream(uri)
            .setChooserTitle("Share GPX")
            .createChooserIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        context.startActivity(intent)
        Log.d("DebugPanel", "Share sheet launched with GPX URI: $uri")
    } else {
        Log.e("DebugPanel", "Failed to export GPX: Uri is null")
    }
}