package varakuta.test_task
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    var gridState = LazyStaggeredGridState(0)
    var searchQuery by mutableStateOf("")
        private set
    val gifs = mutableStateListOf<List<String>>()
    private var gifsOffset by mutableIntStateOf(0)
    var scrollFlag by mutableStateOf(false)
    private val searchTime = 1
    private var searchTimer by mutableIntStateOf(searchTime)
    private var gifsInUpdate by mutableStateOf(false)
    private val apiKey = "Your API key"
    private var timerJob: Job? = null

    fun updateSearchQuery(newQuery: String) {
        if(searchQuery != newQuery) {
            searchQuery = newQuery
            resetSearchTimer()
            updateOffset(0)
            startSearchTimer()
        }
    }
    fun updateOffset(value: Int) {
        gifsOffset = value
        if (searchTimer <= 0 && searchQuery.isNotEmpty()&& !gifsInUpdate) {
            loadGifs(10)
        }
    }

    private fun resetSearchTimer() {
        searchTimer = searchTime
    }

    private fun decrementSearchTimer() {
        if (searchTimer > 0) searchTimer -= 1
    }

    private fun clearGifs() {
        gifs.clear()
    }

    private fun addGifList(newList: List<String>) {
        gifs.add(newList)
    }

    private fun startSearchTimer() {
        timerJob?.cancel()
        resetSearchTimer()
        timerJob = viewModelScope.launch {
            while (searchTimer > 0) {
                delay(1000)
                decrementSearchTimer()
            }
            clearGifs()
            if (searchTimer <= 0 && searchQuery.isNotEmpty() && !gifsInUpdate) {
                scrollFlag = true
                gridState.requestScrollToItem(0)
                loadGifs(30)
            }
        }
    }

    private fun loadGifs(limit:Int) {
        viewModelScope.launch {
            gifsInUpdate=true
            try {
                val response = RetrofitInstance.api.searchGifs(
                    apiKey,
                    searchQuery,
                    limit= limit,
                    offset = gifsOffset
                )
                val count = response.data.size
                for (i in 0 until count) {
                    val gif = response.data[i].images.original
                    addGifList(listOf(gif.url, gif.width, gif.height))
                }
            } catch (_: Exception) {
            }
            gifsInUpdate=false
            scrollFlag = false
        }
    }
}
