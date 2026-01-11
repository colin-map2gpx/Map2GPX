// app/src/main/java/com/colin/map2gpx/util/PolylineDecoder.kt
package com.colin.map2gpx.util

import com.colin.map2gpx.model.LatLng

// Uses your existing LatLng data class:
// data class LatLng(val latitude: Double, val longitude: Double)

object PolylineDecoder {

    /**
     * Decodes a Google encoded polyline string into a list of LatLng points.
     *
     * Reference format:
     * - Encoded string contains ASCII characters that represent variable-length quantities.
     * - Each coordinate is encoded as a delta (difference from previous), scaled by 1e5.
     *
     * @param encoded The encoded polyline string (e.g., from Google Maps route).
     * @return Ordered list of LatLng points along the route.
     */
    fun decode(encoded: String): List<LatLng> {
        if (encoded.isEmpty()) return emptyList()

        val points = ArrayList<LatLng>()
        var index = 0
        var latE5 = 0
        var lngE5 = 0

        while (index < encoded.length) {
            // Decode latitude delta
            val latResult = decodeNextComponent(encoded, index)
            if (latResult == null) break
            latE5 += latResult.value
            index = latResult.nextIndex

            // Decode longitude delta
            val lngResult = decodeNextComponent(encoded, index)
            if (lngResult == null) break
            lngE5 += lngResult.value
            index = lngResult.nextIndex

            // Convert to LatLng (divide by 1e5 to get decimal degrees)
            val lat = latE5 / 1e5.toDouble()
            val lng = lngE5 / 1e5.toDouble()
            points.add(LatLng(lat, lng))
        }

        return points
    }

    /**
     * Helper to decode one value (latitude or longitude delta) from the polyline stream.
     * Consumes characters until a byte with continuation bit cleared is found.
     *
     * @return ComponentResult or null if input is malformed/insufficient.
     */
    private fun decodeNextComponent(encoded: String, startIndex: Int): ComponentResult? {
        var result = 0
        var shift = 0
        var index = startIndex

        // Read characters until we find one without the continuation bit
        while (index < encoded.length) {
            val b = encoded[index].code - 63   // Convert ASCII to value (Google offset)
            index++

            result = result or ((b and 0x1F) shl shift)  // Take 5 bits at a time
            shift += 5

            if (b < 0x20) break  // Stop when continuation bit is cleared
        }

        // If we exited because we ran out of input without clearing continuation bit
        if (shift == 0) return null

        // Zigzag decode: convert to signed int
        val delta = if ((result and 1) != 0) {
            -(result shr 1)
        } else {
            result shr 1
        }

        return ComponentResult(value = delta, nextIndex = index)
    }

    private data class ComponentResult(
        val value: Int,       // delta in E5 integer form (can be negative after zigzag)
        val nextIndex: Int    // where to continue reading in the encoded string
    )
}