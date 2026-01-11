// app/src/main/java/com/colin/map2gpx/ui/DebugPanel.kt
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
            LogUtils.info("DebugPanel: Reading GPX asset: $assetPath")

            val text = runCatching { readAssetText(context, assetPath) }
                .onFailure { LogUtils.error("DebugPanel: Failed to read asset: $assetPath", it) }
                .getOrNull()

            if (text.isNullOrEmpty()) {
                LogUtils.error("DebugPanel: Asset is empty or missing: $assetPath")
                return@Button
            }

            LogUtils.info("DebugPanel: Parsing GPX (${text.length} chars)")
            val (waypoints, trackPoints) = runCatching { GpxParser.parse(text) }
                .onFailure { LogUtils.error("DebugPanel: GPX parse failed", it) }
                .getOrNull() ?: Pair(emptyList(), emptyList())

            LogUtils.info("DebugPanel: Parsed ${waypoints.size} waypoints, ${trackPoints.size} track points")

            // Notify upstream (e.g., for list UI or state)
            onWaypointsLoaded(waypoints)

            val mv = mapView
            if (mv == null) {
                LogUtils.info("DebugPanel: MapView not ready; skipped rendering.")
                return@Button
            }

            // Render both: style is set in renderWaypoints, then reused in renderTrack
            MapRenderer.renderWaypoints(context, mv, waypoints)
            MapRenderer.renderTrack(context, mv, waypoints, trackPoints)

            LogUtils.info("DebugPanel: Rendered ${waypoints.size} waypoints and ${trackPoints.size} track points on map")
        }) {
            Text("Load & Render GPX")
        }
    }
}

private fun readAssetText(context: Context, path: String): String {
    context.assets.open(path).use { input ->
        return input.bufferedReader().readText()
    }
}