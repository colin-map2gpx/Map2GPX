package com.colin.map2gpx.util

import android.util.Log
import com.colin.map2gpx.model.Waypoint

object LogUtils {
    private const val TAG = "map2gpx"

    fun info(message: String) {
        Log.i(TAG, message)
    }

    fun error(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }
    }

    fun waypoint(index: Int, total: Int, w: Waypoint) {
        Log.i(
            TAG,
            "Waypoint [$index/$total] lat=${w.lat}, lon=${w.lon}, icon=${w.icon}, name=${w.name ?: ""}"
        )
    }
}

