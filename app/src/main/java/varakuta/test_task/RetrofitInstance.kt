package varakuta.test_task

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.giphy.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: GiphyApiService by lazy {
        retrofit.create(GiphyApiService::class.java)
    }
}