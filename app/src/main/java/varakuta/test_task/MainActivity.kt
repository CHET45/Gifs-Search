package varakuta.test_task

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import varakuta.test_task.ui.theme.Test_taskTheme


class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val connectivityViewModel: ConnectivityViewModel by viewModels {
        ConnectivityViewModelFactory(applicationContext)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyAppScreen()
        }
    }

    @Composable
    fun MyAppScreen() {
        val isConnectionChanged by connectivityViewModel.isConnectionChanged.collectAsStateWithLifecycle()
        val isConnected by connectivityViewModel.isConnected.collectAsStateWithLifecycle()
        val searchQuery by viewModel::searchQuery
        val tempSearchQuery = remember{ mutableStateOf(searchQuery)}
        val previousConnection = remember { mutableStateOf(isConnectionChanged) }
        val gifs by viewModel::gifs
        val scrollFlag by viewModel::scrollFlag
        val gridState by viewModel::gridState
        val screenMetrics = getScreenMetrics()
        val density = LocalDensity.current.density
        val orientation = getOrientation(screenMetrics)
        val gridCells =calculateGridCells(screenMetrics[0],density,orientation).dp
        val minHeight = 50
        val maxHeight = 500
        val fakeLoadingFields = remember {
            generateFakeLoadingFields(screenMetrics[1],minHeight,maxHeight)
        }
       LaunchedEffect(isConnectionChanged) {
            if(isConnectionChanged != previousConnection.value){
                viewModel.updateSearchQuery(tempSearchQuery.value)
                previousConnection.value = isConnectionChanged
            }
        }
        LaunchedEffect(orientation) {
            if(gridState.firstVisibleItemIndex>10){
                gridState.animateScrollToItem(0)
            }
        }
        Test_taskTheme {
            SimpleSearchBar(viewModel,tempSearchQuery, isConnected)
            LazyVerticalStaggeredGrid(
                verticalItemSpacing = 4.dp,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                columns = StaggeredGridCells.Adaptive(gridCells),
                modifier = Modifier.fillMaxSize(),
                state = gridState
            ) {

                if(gifs.isEmpty()||scrollFlag){
                    if(searchQuery.isNotEmpty()) {
                        items(fakeLoadingFields) { height ->
                            FakeLoadingField(height,screenMetrics,density,orientation)
                        }
                    }
                }
                else {
                    items(gifs) { gif->
                        GifImage(gif, screenMetrics, density, orientation)
                    }
                }
            }
            LaunchedEffect(gridState.firstVisibleItemIndex) {
                if (gridState.firstVisibleItemIndex + gridState.layoutInfo.visibleItemsInfo.size >= gifs.size-3&& gifs.isNotEmpty()) {
                    viewModel.updateOffset(gifs.size)
                }
            }
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SimpleSearchBar(viewModel: MainViewModel, tempSearchQuery: MutableState<String>, isConnected:Boolean) {
        Log.d("KKKK", isConnected.toString())
        SearchBar(
            query = if(isConnected) viewModel.searchQuery else tempSearchQuery.value,
            onQueryChange = {
                if(isConnected) {
                    viewModel.updateSearchQuery(it)
                }
                tempSearchQuery.value=it
            },
            onSearch = {},
            active = false,
            onActiveChange = {},
            modifier = Modifier
                .padding(start = 12.dp, top = 2.dp, end = 12.dp, bottom = 12.dp)
                .fillMaxWidth(),
            placeholder = { Text("Search") }
        ) {}
    }
    @Composable
    fun FakeLoadingField(height:Int,screenMetrics:Array<Int>,density: Float,orientation:String){
        val width = calculateItemWidth(screenMetrics[0],density,orientation)
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .size((width - 10).dp, (height).dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.LightGray)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(min((width/2).dp,(height/2).dp)),
                strokeWidth = 10.dp,
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
    @Composable
    fun GifImage(gif: List<String>,screenMetrics:Array<Int>,density: Float,orientation:String) {
        val context = LocalContext.current
        val width = calculateItemWidth(screenMetrics[0],density,orientation)
        val url = gif.getOrNull(0)
        val originalWidth = gif.getOrNull(1)?.toIntOrNull()
        val originalHeight = gif.getOrNull(2)?.toIntOrNull()
        if (url.isNullOrEmpty() || originalWidth == null || originalHeight == null) return
        val height = calculateImageHeight(originalWidth,originalHeight,width,density)
        val loading = remember(url) { mutableStateOf(true) }
        val error = remember(url) { mutableStateOf(false) }
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
                    .size((width - 10).dp, (height).dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.LightGray)
            ) {
                Text(text = "Error loading gif...")
            }
        } else if (loading.value) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .size((width - 10).dp, height.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.LightGray)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(min((width / 2).dp, (height / 2).dp)),
                    strokeWidth = 10.dp,
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
        AsyncImage(
            model = imageRequest,
            imageLoader = imageLoader,
            contentDescription = null,
            modifier = Modifier
                .size(width.dp, height.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { openDetailedGif(gif) },
            onSuccess = {
                loading.value = false
            },
            onError = {
                error.value = true
            }
        )


    }
    private fun openDetailedGif(gif:List<String>){
        val intent = Intent(this,DetailedGifActivity::class.java)
        intent.putExtra("GIF",gif.toTypedArray())
        startActivity(intent)
    }

}




