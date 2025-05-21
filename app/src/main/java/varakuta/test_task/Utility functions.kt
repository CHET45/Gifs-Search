package varakuta.test_task

import android.app.Activity.WINDOW_SERVICE
import android.graphics.Point
import android.os.Build
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import kotlin.random.Random

@Composable
fun getScreenMetrics(): Array<Int> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        getScreenMetricsForApi30AndAbove()
    } else {
        getScreenMetricsForApi25AndBelow()
    }
}
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun getScreenMetricsForApi30AndAbove(): Array<Int> {
    val context = LocalContext.current
    val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
    val windowMetrics = windowManager.currentWindowMetrics
    val bounds = windowMetrics.bounds
    return arrayOf(bounds.width(), bounds.height())
}
@Composable
fun getScreenMetricsForApi25AndBelow(): Array<Int> {
    val context = LocalContext.current
    @Suppress("DEPRECATION") val display = (context.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
    val point = Point()
    @Suppress("DEPRECATION")
    display.getSize(point)
    return arrayOf(point.x, point.y)
}
fun generateFakeLoadingFields(screenHeight: Int, minHeight: Int, maxHeight: Int): SnapshotStateList<Int> {
    return mutableStateListOf<Int>().apply {
        repeat(screenHeight / minHeight) {
            add(Random.nextInt(minHeight, maxHeight))
        }
    }
}
fun getOrientation(screenMetrics: Array<Int>): String {
    return if (screenMetrics[0] > screenMetrics[1]) "horizontal" else "vertical"
}
fun calculateGridCells(screenWidth: Int, density: Float, orientation: String): Float {
    return if (orientation == "horizontal") {
        (screenWidth / (4 * density) - 4)
    } else {
        (screenWidth / (2 * density) - 4)
    }
}
fun calculateItemWidth(screenWidth: Int, density: Float, orientation: String): Float {
    return if (orientation == "horizontal") {
        (screenWidth / 4f) / density
    } else {
        (screenWidth / 2f) / density
    }
}
fun calculateImageHeight(originalWidth: Int, originalHeight: Int, width: Float, density: Float): Float {
    return (originalHeight * width * density / originalWidth) / density
}
fun scaleToFit(screenMetrics: Array<Int>, width: Int, height: Int, density: Float, orientation: String): Pair<Float, Float> {
    return if (orientation == "horizontal") {
        val finalHeight = screenMetrics[1] / density
        val finalWidth = (width * finalHeight * density / height) / density
        Pair(finalWidth, finalHeight)
    } else {
        val finalWidth = screenMetrics[0] / density
        val finalHeight = (height * finalWidth * density / width) / density
        Pair(finalWidth, finalHeight)
    }
}

