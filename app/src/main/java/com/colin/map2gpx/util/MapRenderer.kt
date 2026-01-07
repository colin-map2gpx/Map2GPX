package com.colin.map2gpx.map

import android.content.Context
import com.colin.map2gpx.model.Waypoint
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.geometry.LatLng          // ✅ FIXED: import LatLng
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLngBounds

object MapRenderer {

    fun renderWaypoints(context: Context, mapView: MapView, waypoints: List<Waypoint>) {
        mapView.getMapAsync { mapLibreMap ->
            // Load OSM Bright style from assets
            mapLibreMap.setStyle(
                Style.Builder().fromUri("asset://osm_bright.json")
            ) { style ->

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

                // Add layer for icons
                val layer = SymbolLayer("waypoints-layer", "waypoints-source").withProperties(
                    iconImage("{icon}"),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true),
                    iconSize(0.5f)
                )
                style.addLayer(layer)

                // Auto-zoom to fit all waypoints
                if (waypoints.isNotEmpty()) {
                    val boundsBuilder = LatLngBounds.Builder()
                    waypoints.forEach { waypoint ->
                        boundsBuilder.include(LatLng(waypoint.lat, waypoint.lon))   // ✅ use include()
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