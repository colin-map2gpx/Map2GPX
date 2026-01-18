package com.colin.map2gpx.ui

import androidx.lifecycle.ViewModel
import com.colin.map2gpx.model.Waypoint
import com.colin.map2gpx.gpx.GpxParser
import org.maplibre.android.geometry.LatLng

class MapViewModel : ViewModel() {

    private val parser = GpxParser

    val waypoints: List<Waypoint>
    val trackPoints: List<LatLng>

    init {
        val (wps, trkpts) = parser.parse(SAMPLE_GPX)
        waypoints = wps
        trackPoints = trkpts
    }

    companion object {
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