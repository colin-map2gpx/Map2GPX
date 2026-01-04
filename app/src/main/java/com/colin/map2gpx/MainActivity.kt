package com.colin.map2gpx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.colin.map2gpx.ui.MapScreen
import com.colin.map2gpx.ui.theme.Map2GpxTheme
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize MapLibre BEFORE creating any MapView
        MapLibre.getInstance(
            applicationContext,
            "dummy-key", // replace with your MapTiler key if needed
            WellKnownTileServer.MapLibre
        )

        setContent {
            Map2GpxTheme {
                MapScreen()
            }
        }
    }
}