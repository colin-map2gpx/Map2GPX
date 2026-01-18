package com.colin.map2gpx.map

import android.content.Context
import android.util.Log
import com.colin.map2gpx.model.Waypoint
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.LineString
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.camera.CameraUpdateFactory

object MapRenderer {

    // Styling constants
    private const val ROUTE_COLOR = "#FF0000"
    private const val ROUTE_WIDTH = 3.0f

    // --- Internal helpers that require a style already loaded ---
    private fun addWaypoints(style: org.maplibre.android.maps.Style, waypoints: List<Waypoint>) {
        if (waypoints.isEmpty()) return

        val features = waypoints.map { wp ->
            Feature.fromGeometry(Point.fromLngLat(wp.lon, wp.lat)).apply {
                wp.icon?.let { addStringProperty("icon", it) }
            }
        }

        val source = style.getSource("waypoints-source") as? GeoJsonSource
        if (source == null) {
            style.addSource(GeoJsonSource("waypoints-source", FeatureCollection.fromFeatures(features)))
        } else {
            source.setGeoJson(FeatureCollection.fromFeatures(features))
        }

        if (style.getLayer("waypoints-layer") == null) {
            val layer = SymbolLayer("waypoints-layer", "waypoints-source").withProperties(
                iconImage("{icon}"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
            )
            style.addLayer(layer)
        }

        Log.d("MapRenderer", "Rendered ${waypoints.size} waypoints")
    }

    private fun addTrack(style: org.maplibre.android.maps.Style, trackPoints: List<LatLng>) {
        if (trackPoints.isEmpty()) return

        val lineFeature = Feature.fromGeometry(
            LineString.fromLngLats(trackPoints.map { Point.fromLngLat(it.longitude, it.latitude) })
        )

        val source = style.getSource("track-source") as? GeoJsonSource
        if (source == null) {
            style.addSource(GeoJsonSource("track-source", FeatureCollection.fromFeature(lineFeature)))
        } else {
            source.setGeoJson(FeatureCollection.fromFeature(lineFeature))
        }

        if (style.getLayer("track-layer") == null) {
            val lineLayer = LineLayer("track-layer", "track-source").withProperties(
                lineColor(ROUTE_COLOR),
                lineWidth(ROUTE_WIDTH)
            )
            // Explicitly place route line below waypoint icons
            style.addLayerBelow(lineLayer, "waypoints-layer")
        }

        Log.d("MapRenderer", "Rendered track with ${trackPoints.size} points")
    }

    private fun zoomToBounds(mapLibreMap: MapLibreMap, waypoints: List<Waypoint>, trackPoints: List<LatLng>) {
        val boundsBuilder = LatLngBounds.Builder()
        waypoints.forEach { boundsBuilder.include(LatLng(it.lat, it.lon)) }
        trackPoints.forEach { boundsBuilder.include(it) }

        try {
            val bounds = boundsBuilder.build()
            val padding = 50 // pixels margin around edges
            mapLibreMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            Log.d("MapRenderer", "Auto-zoomed to bounds covering ${waypoints.size} waypoints + ${trackPoints.size} track points")
        } catch (e: Exception) {
            Log.w("MapRenderer", "zoomToBounds skipped: no points to include")
        }
    }

    // --- Public entry points ---
    fun renderWaypoints(context: Context, mapView: MapView, waypoints: List<Waypoint>) {
        mapView.getMapAsync { mapLibreMap ->
            mapLibreMap.getStyle { style ->
                addWaypoints(style, waypoints)
                zoomToBounds(mapLibreMap, waypoints, emptyList())
            }
        }
    }

    fun renderTrack(context: Context, mapView: MapView, trackPoints: List<LatLng>) {
        mapView.getMapAsync { mapLibreMap ->
            mapLibreMap.getStyle { style ->
                addTrack(style, trackPoints)
                zoomToBounds(mapLibreMap, emptyList(), trackPoints)
            }
        }
    }

    fun renderAll(context: Context, mapView: MapView, waypoints: List<Waypoint>, trackPoints: List<LatLng>) {
        mapView.getMapAsync { mapLibreMap ->
            mapLibreMap.getStyle { style ->
                addWaypoints(style, waypoints)
                addTrack(style, trackPoints)
                zoomToBounds(mapLibreMap, waypoints, trackPoints)
                Log.d("MapRenderer", "Rendered ${waypoints.size} waypoints + ${trackPoints.size} track points")
            }
        }
    }

    // Convenience overload if you already have a MapLibreMap
    fun renderAll(context: Context, mapLibreMap: MapLibreMap, waypoints: List<Waypoint>, trackPoints: List<LatLng>) {
        mapLibreMap.getStyle { style ->
            addWaypoints(style, waypoints)
            addTrack(style, trackPoints)
            zoomToBounds(mapLibreMap, waypoints, trackPoints)
            Log.d("MapRenderer", "Rendered ${waypoints.size} waypoints + ${trackPoints.size} track points")
        }
    }

    // --- Safe clear helpers (toggle visibility without removing sources) ---
    fun hideLayers(mapLibreMap: MapLibreMap) {
        mapLibreMap.getStyle { style ->
            style.getLayer("waypoints-layer")?.setProperties(visibility("none"))
            style.getLayer("track-layer")?.setProperties(visibility("none"))
            Log.d("MapRenderer", "Layers hidden (waypoints + track)")
        }
    }

    fun showLayers(mapLibreMap: MapLibreMap) {
        mapLibreMap.getStyle { style ->
            style.getLayer("waypoints-layer")?.setProperties(visibility("visible"))
            style.getLayer("track-layer")?.setProperties(visibility("visible"))
            Log.d("MapRenderer", "Layers shown (waypoints + track)")
        }
    }
}