package com.myapplications.mywatchlist.data.remote.api

import com.myapplications.mywatchlist.core.util.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

const val apiKey = Constants.API_KEY

interface TmdbApi {

    @GET("search/multi?api_key=$apiKey&include_adult=false")
    suspend fun search(@Query("query") query: String): Response<ApiResponse>

    @GET("genre/movie/list?api_key=$apiKey")
    suspend fun getMovieGenres(): Response<ApiResponse>

    @GET("genre/tv/list?api_key=$apiKey")
    suspend fun getTvGenres(): Response<ApiResponse>

    @GET("trending/all/week?api_key=$apiKey")
    suspend fun getTrendingTitles(): Response<ApiResponse>
}