package com.colin.map2gpx.util

import android.util.Log

object Checkpoint {
    fun start(tag: String, message: String) {
        Log.d(tag, "▶ START: $message")
    }

    fun done(tag: String, message: String) {
        Log.d(tag, "✔ DONE: $message")
    }
}