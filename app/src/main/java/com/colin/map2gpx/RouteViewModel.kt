package com.colin.map2gpx

import androidx.lifecycle.ViewModel
import com.colin.map2gpx.model.LatLng

/**
 * Shared ViewModel to hold the current route and GPX data.
 * This avoids passing large strings or lists through nav arguments.
 */
class RouteViewModel : ViewModel() {

    // Parsed route points
    private var _routePoints: List<LatLng> = emptyList()
    val routePoints: List<LatLng> get() = _routePoints

    // Generated GPX string
    private var _gpxData: String? = null
    val gpxData: String? get() = _gpxData

    fun setRoute(points: List<LatLng>) {
        _routePoints = points
    }

    fun setGpxData(gpx: String) {
        _gpxData = gpx
    }

    fun clear() {
        _routePoints = emptyList()
        _gpxData = null
    }
}

