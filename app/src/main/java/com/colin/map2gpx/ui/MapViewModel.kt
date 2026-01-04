package com.colin.map2gpx.ui

import androidx.lifecycle.ViewModel
import com.colin.map2gpx.model.Waypoint   // ✅ Correct import
import com.colin.map2gpx.gpx.GpxParser

/**
 * ViewModel holds parsed waypoints. On init, parses a built-in sample GPX string
 * so you can confirm the full pipeline without touching MainActivity.
 */
class MapViewModel : ViewModel() {

    private val parser = GpxParser
    val waypoints: List<Waypoint> = parser.parse(SAMPLE_GPX)

    companion object {
        // Minimal embedded GPX with a couple of waypoints
        private const val SAMPLE_GPX = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1" creator="Map2GPX" xmlns="http://www.topografix.com/GPX/1/1">
              <wpt lat="13.7563" lon="100.5018">
                <name>City Camp</name>
                <type>camp</type>
              </wpt>
              <wpt lat="12.5657" lon="104.9910">
                <name>Pump Station</name>
                <type>fuel</type>
              </wpt>
            </gpx>
        """
    }
}