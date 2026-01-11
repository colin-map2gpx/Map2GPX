package com.colin.map2gpx.ui.route

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.colin.map2gpx.model.Waypoint

@Composable
fun RouteScreen(
    waypoints: List<Waypoint>,
    shortLink: String
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Shared Route",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Link: $shortLink",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Waypoints:",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        // ✅ Display each waypoint by its name
        waypoints.forEach { wp ->
            Text(
                text = "- ${wp.name}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}