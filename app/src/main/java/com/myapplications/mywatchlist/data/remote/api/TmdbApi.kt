package com.myapplications.mywatchlist.data.remote.api

import com.myapplications.mywatchlist.core.util.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
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

    @GET("trending/all/week?api_key=$apiKey")
    suspend fun getTrendingTitlesPaging(@Query("page") page: Int): Response<ApiResponse>

    @GET("movie/popular?api_key=$apiKey")
    suspend fun getPopularMovies(): Response<ApiResponse>

    @GET("tv/popular?api_key=$apiKey")
    suspend fun getPopularTV(): Response<ApiResponse>

    @GET("movie/top_rated?api_key=$apiKey")
    suspend fun getTopRatedMovies(): Response<ApiResponse>

    @GET("tv/top_rated?api_key=$apiKey")
    suspend fun getTopRatedTV(): Response<ApiResponse>

    @GET("movie/upcoming?api_key=$apiKey")
    suspend fun getUpcomingMovies(): Response<ApiResponse>

    @GET("movie/{titleId}?api_key=$apiKey&append_to_response=credits,videos,recommendations,similar")
    suspend fun getMovie(@Path("titleId") titleId: Long): Response<ApiResponse>

    @GET("tv/{titleId}?api_key=$apiKey&append_to_response=credits,videos,recommendations,similar")
    suspend fun getTv(@Path("titleId") titleId: Long): Response<ApiResponse>

    @GET("configuration?api_key=$apiKey")
    suspend fun getConfiguration(): Response<ApiResponse>
}