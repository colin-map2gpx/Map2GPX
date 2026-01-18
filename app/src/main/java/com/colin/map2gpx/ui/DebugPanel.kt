package com.colin.map2gpx.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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

@Composable
fun DebugPanel(
    context: Context,
    mapView: MapView?,   // nullable
    modifier: Modifier = Modifier,
    assetPath: String = "sample.gpx",
    currentWaypoints: List<Waypoint> = emptyList(),
    onDataLoaded: (List<Waypoint>, List<LatLng>) -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Button 1: Preview sample route from bundled asset
            Button(onClick = {
                Log.d("DebugPanel", "Reading demo asset: $assetPath")
                val (waypoints, trackPoints) = context.assets.open(assetPath).use { inputStream ->
                    GpxParser.parse(inputStream)
                }
                Log.d("DebugPanel", "Parsed ${waypoints.size} waypoints, ${trackPoints.size} track points")

                mapView?.let { safeMapView ->
                    MapRenderer.renderAll(context, safeMapView, waypoints, trackPoints)
                    Log.d("DebugPanel", "Rendered ${waypoints.size} waypoints + ${trackPoints.size} track points")
                }

                // Propagate both lists to the caller (MapScreen)
                onDataLoaded(waypoints, trackPoints)
            }) {
                Text("Preview Sample Route")
            }

            // Button 2: Load Route from Share (stub for now)
            Button(onClick = {
                Log.d("DebugPanel", "TODO: Handle Load Route from Share flow")
                // Later: integrate with Share Sheet or SAF to load GPX route
            }) {
                Text("Load Route from Share")
            }

            // Button 3: Export & Share GPX (uses currentWaypoints)
            Button(onClick = {
                if (currentWaypoints.isEmpty()) {
                    Log.w("DebugPanel", "No waypoints to export. Load or preview a route first.")
                } else {
                    shareGpx(context, currentWaypoints)
                }
            }) {
                Text("Export & Share GPX")
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