// app/src/main/java/com/colin/map2gpx/data/WaypointRepository.kt
package com.colin.map2gpx.data

import com.colin.map2gpx.model.Waypoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object WaypointRepository {
    private val _waypoints = MutableStateFlow<List<Waypoint>>(emptyList())
    val waypoints: StateFlow<List<Waypoint>> = _waypoints

    fun setWaypoints(items: List<Waypoint>) {
        _waypoints.value = items
    }

    fun addWaypoint(item: Waypoint) {
        _waypoints.value = _waypoints.value + item
    }

    fun clear() {
        _waypoints.value = emptyList()
    }
}