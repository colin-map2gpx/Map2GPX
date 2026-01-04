package com.colin.map2gpx.parser

import com.colin.map2gpx.model.LatLng

object UrlParser {

    /**
     * Parse a Google Maps URL and return a list of LatLng points.
     * If the URL contains an encoded polyline, it will be decoded.
     * Otherwise, a single LatLng may be extracted.
     */
    fun parse(url: String): List<LatLng> {
        // Try to extract polyline from "pb=" parameter
        val polylineRegex = Regex("pb=([^&]+)")
        val polylineMatch = polylineRegex.find(url)
        if (polylineMatch != null) {
            val encoded = polylineMatch.groupValues[1]
            return decodePolyline(encoded)
        }

        // Fallback: try to extract a single lat/lng from the URL
        val latLngRegex = Regex("!3d(-?\\d+\\.\\d+)!4d(-?\\d+\\.\\d+)")
        val latLngMatch = latLngRegex.find(url)
        if (latLngMatch != null) {
            val lat = latLngMatch.groupValues[1].toDouble()
            val lng = latLngMatch.groupValues[2].toDouble()
            return listOf(LatLng(lat, lng))
        }

        // If nothing found, return empty list
        return emptyList()
    }

    /**
     * Decode a Google Maps encoded polyline string into a list of LatLng points.
     */
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

            val latLng = LatLng(lat / 1E5, lng / 1E5)
            poly.add(latLng)
        }
        return poly
    }
}