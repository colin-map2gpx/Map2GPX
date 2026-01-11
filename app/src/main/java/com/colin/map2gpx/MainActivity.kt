package com.colin.map2gpx

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.colin.map2gpx.model.Waypoint
import com.colin.map2gpx.ui.route.RouteScreen   // ✅ use your updated RouteScreen
import com.colin.map2gpx.ui.theme.Map2GpxTheme
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import com.colin.map2gpx.util.RouteExtractor

class MainActivity : ComponentActivity() {

    private var sharedText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize MapLibre BEFORE creating any MapView
        MapLibre.getInstance(
            applicationContext,
            "dummy-key", // replace with MapTiler key if needed
            WellKnownTileServer.MapLibre
        )

        // Handle initial share intent
        sharedText = handleShareIntent(intent)
        Log.d("RouteShare", "onCreate() called with intent: $intent")

        if (sharedText != null) {
            Log.d("RouteShare", "Received shared route (onCreate): $sharedText")

            // Expand and parse the route asynchronously
            RouteExtractor.expandShortUrlAsync(this, sharedText!!) { expanded ->
                Log.d("RouteShare", "Expanded URL (onCreate): $expanded")
                if (expanded == null) {
                    Toast.makeText(this, "Couldn’t resolve the shared link.", Toast.LENGTH_SHORT).show()
                    return@expandShortUrlAsync
                }

                val waypoints: List<Waypoint> = RouteExtractor.parseWaypoints(expanded)
                Log.d("RouteShare", "Parsed waypoints (onCreate): $waypoints")

                setContent {
                    Map2GpxTheme {
                        RouteScreen(
                            waypoints = waypoints,
                            shortLink = expanded
                        )
                    }
                }
            }
        } else {
            Log.d("RouteShare", "No shared route found in onCreate intent")
            setContent {
                Map2GpxTheme {
                    // fallback screen if no route shared
                    RouteScreen(
                        waypoints = emptyList<Waypoint>(),   // ✅ typed empty list
                        shortLink = ""
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // update stored intent
        Log.d("RouteShare", "onNewIntent() called with intent: $intent")

        val newSharedText = handleShareIntent(intent)
        if (newSharedText != null) {
            Log.d("RouteShare", "Received new shared route (onNewIntent): $newSharedText")
            sharedText = newSharedText

            // Expand and parse the route asynchronously
            RouteExtractor.expandShortUrlAsync(this, sharedText!!) { expanded ->
                Log.d("RouteShare", "Expanded URL (onNewIntent): $expanded")
                if (expanded == null) {
                    Toast.makeText(this, "Couldn’t resolve the shared link.", Toast.LENGTH_SHORT).show()
                    return@expandShortUrlAsync
                }
                val waypoints: List<Waypoint> = RouteExtractor.parseWaypoints(expanded)
                Log.d("RouteShare", "Parsed waypoints (onNewIntent): $waypoints")

                setContent {
                    Map2GpxTheme {
                        RouteScreen(
                            waypoints = waypoints,
                            shortLink = expanded
                        )
                    }
                }
            }
        } else {
            Log.d("RouteShare", "No shared route found in new intent")
        }
    }

    private fun handleShareIntent(intent: Intent): String? {
        Log.d("RouteShare", "handleShareIntent() action=${intent.action}, type=${intent.type}")

        return when {
            intent.action == Intent.ACTION_SEND && intent.type == "text/plain" -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                Log.d("RouteShare", "Extracted EXTRA_TEXT (plain): $text")
                text
            }
            intent.action == Intent.ACTION_SEND && intent.type == "text/uri-list" -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                Log.d("RouteShare", "Extracted EXTRA_TEXT (uri-list): $text")
                text
            }
            intent.action == Intent.ACTION_VIEW -> {
                val data = intent.dataString
                Log.d("RouteShare", "Extracted dataString (VIEW): $data")
                data
            }
            else -> {
                Log.d("RouteShare", "Intent did not match any known share/view pattern")
                null
            }
        }
    }
}