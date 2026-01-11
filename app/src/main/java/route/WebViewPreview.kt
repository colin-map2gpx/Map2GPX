// app/src/main/java/com/colin/map2gpx/ui/route/WebViewPreview.kt
package com.colin.map2gpx.ui.route

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.Icons              // <-- correct import
import androidx.compose.material.icons.filled.Refresh  // <-- correct import

@Composable
fun WebViewPreview(url: String) {
    var progress by remember { mutableStateOf(0) }
    var canGoBack by remember { mutableStateOf(false) }
    var webView: WebView? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress
                            canGoBack = view?.canGoBack() == true
                        }
                    }
                    settings.javaScriptEnabled = true
                    loadUrl(url)
                    webView = this
                }
            }
        )

        // Show progress bar while loading
        if (progress in 1..99) {
            LinearProgressIndicator(
                progress = progress / 100f,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Reload button
        FloatingActionButton(
            onClick = { webView?.reload() },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = "Reload") // <-- fixed usage
        }
    }

    // Handle back button
    BackHandler(enabled = canGoBack) {
        webView?.goBack()
    }
}