package com.colin.map2gpx.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * Resolves short links (e.g., https://maps.app.goo.gl/...) by loading them in a hidden WebView
 * and capturing the final navigated URL once Google’s JavaScript/meta refresh completes.
 *
 * Usage:
 * WebViewResolver.resolve(context, shortUrl, timeoutMs = 8000) { finalUrl ->
 *     // finalUrl may be null on timeout or failure
 * }
 */
object WebViewResolver {

    fun resolve(
        context: Context,
        shortUrl: String,
        timeoutMs: Long = 8000,
        onResult: (String?) -> Unit
    ) {
        val webView = WebView(context)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadsImagesAutomatically = false
        webView.settings.setSupportZoom(false)

        var resolvedUrl: String? = null
        var finished = false

        val handler = Handler(Looper.getMainLooper())
        val timeout = Runnable {
            if (!finished) {
                Log.w("RouteShare", "WebView resolve timeout")
                finished = true
                safeDestroy(webView)
                onResult(resolvedUrl) // may be null
            }
        }
        handler.postDelayed(timeout, timeoutMs)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString()
                if (url != null) {
                    Log.d("RouteShare", "WebView navigating: $url")
                    // Capture when we hit a Google Maps URL
                    if (isFinalMapsUrl(url)) {
                        resolvedUrl = url
                        if (!finished) {
                            finished = true
                            handler.removeCallbacks(timeout)
                            safeDestroy(webView)
                            onResult(resolvedUrl)
                        }
                        return true // stop further loading
                    }
                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (url != null) {
                    Log.d("RouteShare", "WebView started: $url")
                    if (isFinalMapsUrl(url)) {
                        resolvedUrl = url
                        if (!finished) {
                            finished = true
                            handler.removeCallbacks(timeout)
                            safeDestroy(webView)
                            onResult(resolvedUrl)
                        }
                    }
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (url != null) {
                    Log.d("RouteShare", "WebView finished: $url")
                    if (isFinalMapsUrl(url)) {
                        resolvedUrl = url
                        if (!finished) {
                            finished = true
                            handler.removeCallbacks(timeout)
                            safeDestroy(webView)
                            onResult(resolvedUrl)
                        }
                    }
                }
            }
        }

        // Start loading the shortlink
        Log.d("RouteShare", "WebView resolving shortlink: $shortUrl")
        webView.loadUrl(shortUrl)
    }

    private fun isFinalMapsUrl(url: String): Boolean {
        // Accept both /maps/dir/... and ?api=1&origin=... formats
        return (url.contains("https://www.google.com/maps/dir/") ||
                (url.startsWith("https://www.google.com/maps") && url.contains("api=1")))
    }

    private fun safeDestroy(webView: WebView) {
        try {
            webView.stopLoading()
            webView.clearHistory()
            webView.clearCache(true)
            webView.destroy()
        } catch (e: Exception) {
            Log.w("RouteShare", "WebView destroy error: ${e.message}")
        }
    }
}