package com.colin.map2gpx.ui

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.colin.map2gpx.gpx.GpxParser
import com.colin.map2gpx.model.Waypoint
import com.colin.map2gpx.util.LogUtils
import com.colin.map2gpx.map.MapRenderer
import org.maplibre.android.maps.MapView

@Composable
fun DebugPanel(
    context: Context,
    mapView: MapView?,
    modifier: Modifier = Modifier,
    assetPath: String = "sample.gpx",
    onWaypointsLoaded: (List<Waypoint>) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = {
            LogUtils.info("Debug: Reading GPX asset: $assetPath")
            val text = runCatching { readAssetText(context, assetPath) }
                .onFailure { LogUtils.error("Failed to read asset: $assetPath", it) }
                .getOrNull()

            if (text.isNullOrEmpty()) {
                LogUtils.error("Asset is empty or missing: $assetPath")
                return@Button
            }

            LogUtils.info("Debug: Parsing GPX (${text.length} chars)")
            val waypoints: List<Waypoint> = runCatching { GpxParser.parse(text) }
                .onFailure { LogUtils.error("GPX parse failed", it) }
                .getOrNull().orEmpty()

            LogUtils.info("Debug: Parsed ${waypoints.size} waypoints")

            onWaypointsLoaded(waypoints)

            mapView?.let { mv ->
                MapRenderer.renderWaypoints(context, mv, waypoints)
                LogUtils.info("Debug: Rendered ${waypoints.size} waypoints on map")
            } ?: LogUtils.info("MapView not ready; skipped rendering.")
        }) {
            Text("Load & Render GPX waypoints")
        }
    }
}

private fun readAssetText(context: Context, path: String): String {
    context.assets.open(path).use { input ->
        return input.bufferedReader().readText()
    }
}