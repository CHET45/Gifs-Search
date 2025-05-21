package varakuta.test_task
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest

class DetailedGifActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val screenMetrics = getScreenMetrics()
            val density = LocalDensity.current.density
            val orientation = getOrientation(screenMetrics)
            val context = LocalContext.current
            val gif = intent.getStringArrayExtra("GIF")
            if(!gif.isNullOrEmpty()) {
                val url = gif.getOrNull(0)
                val originalWidth = gif.getOrNull(1)?.toIntOrNull()
                val originalHeight = gif.getOrNull(2)?.toIntOrNull()
                if (!url.isNullOrEmpty() && originalWidth != null && originalHeight != null) {
                    val loading = remember(url) { mutableStateOf(true) }
                    val error = remember(url) { mutableStateOf(false) }
                    val scaledGifSize = scaleToFit(screenMetrics, originalWidth, originalHeight, density, orientation)
                    val width = scaledGifSize.first
                    val height = scaledGifSize.second
                    val imageLoader = remember {
                        ImageLoader.Builder(context)
                            .components {
                                if (Build.VERSION.SDK_INT >= 28) {
                                    add(ImageDecoderDecoder.Factory())
                                } else {
                                    add(GifDecoder.Factory())
                                }
                            }
                            .build()
                    }
                    val imageRequest = remember(url) {
                        ImageRequest.Builder(context)
                            .data(url)
                            .crossfade(true)
                            .build()
                    }
                    if (error.value) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray)
                        ) {
                            Text(text = "Error loading gif...")
                        }
                    } else if (loading.value) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size((screenMetrics.min()/10).dp),
                                strokeWidth = 10.dp,
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                    }
                    Box(contentAlignment = Alignment.Center) {
                        Button(
                            onClick = { finish() },
                            modifier = Modifier
                                .fillMaxSize(),
                            shape = RectangleShape,
                            colors = ButtonColors(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Transparent,
                                Color.Transparent
                            )
                        ) {}
                        AsyncImage(
                            model = imageRequest,
                            imageLoader = imageLoader,
                            contentDescription = null,
                            modifier = Modifier
                                .size(width.dp, height.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = {})
                                },
                            onSuccess = {
                                loading.value = false
                            },
                            onError = {
                                error.value = true
                            }
                        )
                    }
                }
            }
        }
    }
}