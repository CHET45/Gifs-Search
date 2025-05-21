package varakuta.test_task

data class GiphyResponse(
    val data: List<GifObject>
)

data class GifObject(
    val images: Images
)

data class Images(
    val original: OriginalImage
)

data class OriginalImage(
    val url: String,
    val width: String,
    val height: String
)

