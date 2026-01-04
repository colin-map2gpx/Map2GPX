package com.colin.map2gpx
import com.colin.map2gpx.gpx.runParserSmokeTest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// ✅ Main Activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }

        // --- GPX parser smoke test ---
        runParserSmokeTest(this)
    }
}



// ✅ Navigation setup
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "map"
    ) {
        composable("map") {
            MapScreen(
                onNavigateToParser = { navController.navigate("parser") }
            )
        }
        composable("parser") {
            ParseGoogleUrlScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// ✅ GPX save + share helper function
fun generateAndShareGpx(context: android.content.Context) {
    val gpxData = """
        <?xml version="1.0" encoding="UTF-8"?>
        <gpx version="1.1" creator="Map2GPX">
            <wpt lat="13.4125" lon="103.8667">
                <name>Angkor Wat</name>
            </wpt>
        </gpx>
    """.trimIndent()

    val fileName = "route.gpx"
    val file = File(context.getExternalFilesDir(null), fileName)
    file.writeText(gpxData)

    val gpxUri: Uri = FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.provider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/gpx+xml"
        putExtra(Intent.EXTRA_STREAM, gpxUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share GPX file"))
}
