// app/src/main/java/com/colin/map2gpx/map/MapRenderer.kt
package com.colin.map2gpx.map

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import com.colin.map2gpx.model.Waypoint
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.LineString
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLngBounds

object MapRenderer {

    fun renderWaypoints(context: Context, mapView: MapView, waypoints: List<Waypoint>) {
        mapView.getMapAsync { mapLibreMap ->
            // Load OSM Bright style from assets
            mapLibreMap.setStyle(
                Style.Builder().fromUri("asset://osm_bright.json")
            ) { style ->

                // ✅ Register icons before adding SymbolLayer
                val iconIds = listOf("tent", "fuel", "motorcycle", "camera", "wrench", "food", "hotel")
                iconIds.forEach { id ->
                    val resId = context.resources.getIdentifier(id, "drawable", context.packageName)
                    if (resId != 0) {
                        val bitmap = BitmapFactory.decodeResource(context.resources, resId)
                        style.addImageAsync(id, bitmap)   // ✅ register icon
                    }
                }

                // Convert waypoints to GeoJSON features
                val features = waypoints.map { waypoint ->
                    Feature.fromGeometry(
                        Point.fromLngLat(waypoint.lon, waypoint.lat)
                    ).apply {
                        addStringProperty("icon", waypoint.icon)
                    }
                }

                // Add source
                val source = GeoJsonSource("waypoints-source", FeatureCollection.fromFeatures(features))
                style.addSource(source)

                // Add layer for icons (✅ use Expression.get)
                val layer = SymbolLayer("waypoints-layer", "waypoints-source").withProperties(
                    iconImage(Expression.get("icon")),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true),
                    iconSize(0.7f),
                    iconHaloColor(Color.WHITE),   // ✅ outline around icon
                    iconHaloWidth(2.0f)           // ✅ thickness of outline
                )
                style.addLayer(layer)
            }
        }
    }

    fun renderTrack(context: Context, mapView: MapView, waypoints: List<Waypoint>, trackPoints: List<LatLng>) {
        mapView.getMapAsync { mapLibreMap ->
            mapLibreMap.getStyle { style ->
                // Render track line if present
                if (trackPoints.isNotEmpty()) {
                    val lineString = LineString.fromLngLats(
                        trackPoints.map { Point.fromLngLat(it.longitude, it.latitude) }
                    )
                    val lineFeature = Feature.fromGeometry(lineString)

                    val lineSource = GeoJsonSource("track-source", FeatureCollection.fromFeature(lineFeature))
                    style.addSource(lineSource)

                    val lineLayer = LineLayer("track-layer", "track-source").withProperties(
                        lineColor(Color.BLUE),
                        lineWidth(3.0f)
                    )
                    style.addLayer(lineLayer)
                }

                // ✅ Unified auto-zoom: fit both waypoints and track points
                if (waypoints.isNotEmpty() || trackPoints.isNotEmpty()) {
                    val boundsBuilder = LatLngBounds.Builder()
                    waypoints.forEach { wp ->
                        boundsBuilder.include(LatLng(wp.lat, wp.lon))
                    }
                    trackPoints.forEach { tp ->
                        boundsBuilder.include(tp)
                    }
                    val bounds = boundsBuilder.build()
                    mapLibreMap.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(bounds, 50)
                    )
                }
            }
        }
    }
}