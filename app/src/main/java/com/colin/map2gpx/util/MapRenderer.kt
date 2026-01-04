package com.colin.map2gpx.util

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.colin.map2gpx.model.Waypoint
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng

object MapRenderer {
    private const val LOG_TAG = "MapRenderer"
    private const val SOURCE_ID = "waypoints-source"
    private const val LAYER_ID = "waypoints-layer"
    private const val ICON_PROPERTY = "icon"

    fun renderWaypoints(
        context: Context,
        mapView: MapView,
        waypoints: List<Waypoint>
    ) {
        mapView.getMapAsync { map: MapLibreMap ->
            map.getStyle { style ->
                ensureIcons(context, style, waypoints)
                addOrUpdateSource(style, waypoints)
                addOrUpdateLayer(style)
                autoZoom(map, waypoints)
                Log.i(LOG_TAG, "Rendered ${waypoints.size} waypoints via SymbolLayer")
            }
        }
    }

    private fun ensureIcons(context: Context, style: Style, waypoints: List<Waypoint>) {
        val uniqueIcons = waypoints.mapNotNull { it.icon }.toSet()
        uniqueIcons.forEach { iconName ->
            val resId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
            if (resId != 0) {
                val bmp = BitmapFactory.decodeResource(context.resources, resId)
                if (style.getImage(iconName) == null) {
                    style.addImage(iconName, bmp)
                    Log.d(LOG_TAG, "Added style image: $iconName")
                }
            } else {
                Log.w(LOG_TAG, "Drawable not found for icon name: $iconName")
            }
        }
    }

    private fun addOrUpdateSource(style: Style, waypoints: List<Waypoint>) {
        val features = waypoints.map { w ->
            Feature.fromGeometry(Point.fromLngLat(w.lon, w.lat)).apply {
                w.icon?.let { addStringProperty(ICON_PROPERTY, it) }
            }
        }
        val collection = FeatureCollection.fromFeatures(features)

        val existing = style.getSource(SOURCE_ID) as? GeoJsonSource
        if (existing == null) {
            val source = GeoJsonSource(SOURCE_ID, collection)
            style.addSource(source)
            Log.d(LOG_TAG, "Added GeoJsonSource: $SOURCE_ID")
        } else {
            existing.setGeoJson(collection)
            Log.d(LOG_TAG, "Updated GeoJsonSource: $SOURCE_ID (${waypoints.size} features)")
        }
    }

    private fun addOrUpdateLayer(style: Style) {
        val existing = style.getLayer(LAYER_ID) as? SymbolLayer
        if (existing == null) {
            val layer = SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
                PropertyFactory.iconImage("{${ICON_PROPERTY}}"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true)
            )
            style.addLayer(layer)
            Log.d(LOG_TAG, "Added SymbolLayer: $LAYER_ID")
        } else {
            existing.setProperties(
                PropertyFactory.iconImage("{${ICON_PROPERTY}}"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true)
            )
        }
    }

    private fun autoZoom(map: MapLibreMap, waypoints: List<Waypoint>) {
        val first = waypoints.firstOrNull() ?: return
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(first.lat, first.lon))
            .zoom(14.0)
            .build()
        map.cameraPosition = cameraPosition
    }
}