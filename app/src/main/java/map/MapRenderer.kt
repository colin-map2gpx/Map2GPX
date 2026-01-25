package com.colin.map2gpx.map

import android.content.Context
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import com.colin.map2gpx.model.Waypoint
import com.colin.map2gpx.util.resolveIconFor

object MapRenderer {

    private const val WAYPOINT_SOURCE_ID = "waypoint-source"
    private const val WAYPOINT_LAYER_ID = "waypoint-layer"
    private const val ROUTE_SOURCE_ID = "route-source"
    private const val ROUTE_LAYER_ID = "route-layer"

    private const val ROUTE_COLOR = "#FF0000"
    private const val ROUTE_WIDTH = 4.0f

    // Simple init for MapScreen
    fun initMap(context: Context, mapView: MapView) {
        mapView.getMapAsync { map ->
            map.setStyle(Style.Builder().fromUri("asset://osm_bright.json"))
        }
    }

    fun renderAll(context: Context, mapView: MapView, waypoints: List<Waypoint>, trackPoints: List<Point>) {
        mapView.getMapAsync { map ->
            map.setStyle(Style.Builder().fromUri("asset://osm_bright.json")) { style ->

                // --- Waypoints ---
                val waypointFeatures = waypoints.map {
                    Feature.fromGeometry(Point.fromLngLat(it.lon, it.lat)).apply {
                        // FIX: pass string icon field, not whole Waypoint
                        addStringProperty("icon", resolveIconFor(it.icon))
                    }
                }
                val waypointSource = style.getSource(WAYPOINT_SOURCE_ID) as? GeoJsonSource
                    ?: GeoJsonSource(WAYPOINT_SOURCE_ID, FeatureCollection.fromFeatures(waypointFeatures)).also {
                        style.addSource(it)
                    }
                waypointSource.setGeoJson(FeatureCollection.fromFeatures(waypointFeatures))

                if (style.getLayer(WAYPOINT_LAYER_ID) == null) {
                    val symbolLayer = SymbolLayer(WAYPOINT_LAYER_ID, WAYPOINT_SOURCE_ID).withProperties(
                        iconImage("{icon}"),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true)
                    )
                    style.addLayer(symbolLayer)
                }

                // --- Route line ---
                val routeFeatures = listOf(Feature.fromGeometry(org.maplibre.geojson.LineString.fromLngLats(trackPoints)))
                val routeSource = style.getSource(ROUTE_SOURCE_ID) as? GeoJsonSource
                    ?: GeoJsonSource(ROUTE_SOURCE_ID, FeatureCollection.fromFeatures(routeFeatures)).also {
                        style.addSource(it)
                    }
                routeSource.setGeoJson(FeatureCollection.fromFeatures(routeFeatures))

                if (style.getLayer(ROUTE_LAYER_ID) == null) {
                    val lineLayer = LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID).withProperties(
                        lineColor(ROUTE_COLOR),
                        lineWidth(ROUTE_WIDTH)
                    )
                    style.addLayerBelow(lineLayer, WAYPOINT_LAYER_ID)
                }
            }
        }
    }
}