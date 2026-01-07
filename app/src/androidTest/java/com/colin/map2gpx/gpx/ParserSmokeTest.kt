package com.colin.map2gpx.gpx

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import com.colin.map2gpx.model.Waypoint
import org.maplibre.android.geometry.LatLng

/**
 * Simple smoke test for the GPX parser.
 * Loads sample.gpx from assets and logs parsed waypoints and track points.
 */
class ParserSmokeTest {

    private companion object {
        private const val LOG_TAG = "ParserSmokeTest"
    }

    @Test
    fun testParseSampleGpx() {
        val context: Context = ApplicationProvider.getApplicationContext()

        // ✅ Destructure the Pair into waypoints and trackPoints
        val (waypoints, trackPoints) = GpxParser.parseGpxFile(context, "sample.gpx")

        Log.i(LOG_TAG, "Parsed ${waypoints.size} waypoints, ${trackPoints.size} track points")
        waypoints.forEach { wp ->
            Log.i(LOG_TAG, "WP icon=${wp.icon} lat=${wp.lat} lon=${wp.lon} name=${wp.name}")
        }
        trackPoints.forEachIndexed { idx, tp ->
            Log.i(LOG_TAG, "TRK[$idx] lat=${tp.latitude} lon=${tp.longitude}")
        }

        assert(waypoints.isNotEmpty()) {
            "Expected at least one waypoint in sample.gpx"
        }
    }
}