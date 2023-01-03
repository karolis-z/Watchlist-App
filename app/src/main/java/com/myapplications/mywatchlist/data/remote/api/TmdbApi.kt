package com.myapplications.mywatchlist.data.remote.api

import com.myapplications.mywatchlist.core.util.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

const val apiKey = Constants.API_KEY

interface TmdbApi {

    @GET("search/multi?api_key=$apiKey&query={query}&include_adult=false")
    suspend fun search(@Path("query") query: String): Response<SearchApiResponse>

}