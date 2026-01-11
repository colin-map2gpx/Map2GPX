package com.colin.map2gpx.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import java.io.File

@Composable
fun MapScreen(sharedRoute: String? = null) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = rememberMapViewWithLifecycle(context, lifecycleOwner)

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        DebugPanel(
            context = context,
            mapView = mapView,
            modifier = Modifier.align(Alignment.BottomCenter),
            onWaypointsLoaded = { /* optional */ }
        )
    }

    // Initialize map style once the map is ready
    mapView.getMapAsync { map ->
        Log.d("MapScreen", "MapView ready → attempting to load style from assets")
        try {
            // Canonical asset URI; ensure app/src/main/assets/osm_bright.json exists
            map.setStyle(
                Style.Builder()
                    .fromUri("asset://osm_bright.json")
            ) { style ->
                Log.d("MapScreen", "Style callback fired → style loaded successfully")
                // Ensure a base source exists so route layers render on top even if style is minimal
                ensureBaseSource(style)

                if (sharedRoute != null) {
                    Log.d("MapScreen", "Shared route received: $sharedRoute")
                    processSharedRoute(context, mapView, sharedRoute)
                }
            }
        } catch (e: Exception) {
            Log.e("MapScreen", "Exception while loading style: ${e.message}", e)
            // Fallback minimal inline style to guarantee a visible background
            map.setStyle(
                Style.Builder().fromJson(
                    """
                    {
                      "version": 8,
                      "name": "Fallback OSM Raster",
                      "sources": {
                        "osm-tiles": {
                          "type": "raster",
                          "tiles": ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
                          "tileSize": 256
                        }
                      },
                      "layers": [
                        { "id": "background", "type": "background", "paint": { "background-color": "#e0e0e0" } },
                        { "id": "osm-tiles", "type": "raster", "source": "osm-tiles", "minzoom": 0, "maxzoom": 19 }
                      ]
                    }
                    """.trimIndent()
                )
            ) { style ->
                Log.d("MapScreen", "Fallback style loaded")
                ensureBaseSource(style)
                if (sharedRoute != null) {
                    Log.d("MapScreen", "Shared route received (fallback): $sharedRoute")
                    processSharedRoute(context, mapView, sharedRoute)
                }
            }
        }
    }
}

// --- Lifecycle helper ---

@Composable
private fun rememberMapViewWithLifecycle(
    context: Context,
    lifecycleOwner: LifecycleOwner
): MapView {
    val mapView = remember { MapView(context.applicationContext) }
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }
    return mapView
}

// --- Style helpers ---

private fun ensureBaseSource(style: Style) {
    // No-op placeholder for future base sources if needed.
    // Keeps a single place to attach any required sources the route depends on.
    Log.d("MapScreen", "ensureBaseSource() invoked")
}

// --- Pipeline functions ---

private fun extractEncodedPolyline(sharedText: String): String? {
    val encRegex = Regex("enc:([^:]+):")
    encRegex.find(sharedText)?.let { return it.groupValues[1] }
    return null
}

data class LatLng(val latitude: Double, val longitude: Double)

private fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
        lng += dlng

        val latitude = lat / 1E5
        val longitude = lng / 1E5
        poly.add(LatLng(latitude, longitude))
    }
    return poly
}

private fun latLngsToFeatureCollection(latLngs: List<LatLng>): FeatureCollection {
    val points = latLngs.map { Point.fromLngLat(it.longitude, it.latitude) }
    val line = LineString.fromLngLats(points)
    val feature = Feature.fromGeometry(line)
    return FeatureCollection.fromFeatures(listOf(feature))
}

private fun renderRoute(mapView: MapView, fc: FeatureCollection) {
    mapView.getMapAsync { mapLibreMap ->
        mapLibreMap.getStyle { style ->
            val existing = style.getSource("route-source") as? GeoJsonSource
            if (existing != null) {
                existing.setGeoJson(fc)
                Log.d("MapScreen", "Updated existing route-source with new GeoJSON")
            } else {
                style.addSource(GeoJsonSource("route-source", fc))
                Log.d("MapScreen", "Added route-source GeoJSON")
            }

            if (style.getLayer("route-layer") == null) {
                val layer = LineLayer("route-layer", "route-source").withProperties(
                    PropertyFactory.lineColor("#2E7D32"),
                    PropertyFactory.lineWidth(4.0f),
                    PropertyFactory.lineOpacity(0.9f)
                )
                style.addLayer(layer)
                Log.d("MapScreen", "Added route-layer")
            } else {
                Log.d("MapScreen", "route-layer already exists")
            }
        }
    }
}

private fun fitRouteBounds(mapView: MapView, latLngs: List<LatLng>, padding: Int = 60) {
    if (latLngs.isEmpty()) return
    val builder = LatLngBounds.Builder()
    latLngs.forEach { builder.include(org.maplibre.android.geometry.LatLng(it.latitude, it.longitude)) }
    val bounds = builder.build()

    mapView.getMapAsync { mapLibreMap ->
        mapLibreMap.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        Log.d("MapScreen", "Camera eased to route bounds")
    }
}

private fun latLngsToGpx(latLngs: List<LatLng>, name: String = "Google Route"): String {
    val sb = StringBuilder()
    sb.append("""<?xml version="1.0" encoding="UTF-8"?>""").append('\n')
    sb.append("""<gpx version="1.1" creator="Map2GPX" xmlns="http://www.topografix.com/GPX/1/1">""").append('\n')
    sb.append("  <trk>\n")
    sb.append("    <name>").append(name).append("</name>\n")
    sb.append("    <trkseg>\n")
    for (pt in latLngs) {
        sb.append("      <trkpt lat=\"")
            .append(pt.latitude)
            .append("\" lon=\"")
            .append(pt.longitude)
            .append("\"></trkpt>\n")
    }
    sb.append("    </trkseg>\n")
    sb.append("  </trk>\n")
    sb.append("</gpx>\n")
    return sb.toString()
}

private fun shareGpx(context: Context, gpxContent: String, fileName: String = "route.gpx") {
    val cacheDir = File(context.cacheDir, "gpx")
    if (!cacheDir.exists()) cacheDir.mkdirs()
    val file = File(cacheDir, fileName)
    file.writeText(gpxContent)

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/gpx+xml"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share GPX"))
    Log.d("MapScreen", "Shared GPX via chooser: $fileName")
}

private fun processSharedRoute(context: Context, mapView: MapView, sharedText: String) {
    val encoded = extractEncodedPolyline(sharedText)
    if (encoded.isNullOrBlank()) {
        Log.e("MapScreen", "No encoded polyline found in shared text.")
        return
    }

    val latLngs = decodePolyline(encoded)
    if (latLngs.isEmpty()) {
        Log.e("MapScreen", "Decoded polyline is empty.")
        return
    }

    val fc = latLngsToFeatureCollection(latLngs)
    renderRoute(mapView, fc)
    fitRouteBounds(mapView, latLngs)

    val gpx = latLngsToGpx(latLngs, name = "Shared Google Route")
    shareGpx(context, gpx, fileName = "google_route.gpx")
}