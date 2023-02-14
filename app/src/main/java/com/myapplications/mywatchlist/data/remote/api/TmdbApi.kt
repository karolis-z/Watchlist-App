package com.myapplications.mywatchlist.data.remote.api

import com.myapplications.mywatchlist.core.util.Constants
import com.myapplications.mywatchlist.domain.entities.SortByParameter
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

const val apiKey = Constants.API_KEY

interface TmdbApi {

    @GET("search/multi?api_key=$apiKey&include_adult=false")
    suspend fun searchAll(
        @Query("query") query: String,
        @Query("page") page: Int
    ): Response<ApiResponse>

    @GET("search/movie?api_key=$apiKey&include_adult=false")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int
    ): Response<ApiResponse>

    @GET("search/tv?api_key=$apiKey&include_adult=false")
    suspend fun searchTV(
        @Query("query") query: String,
        @Query("page") page: Int
    ): Response<ApiResponse>

    @GET("genre/movie/list?api_key=$apiKey")
    suspend fun getMovieGenres(): Response<ApiResponse>

    @GET("genre/tv/list?api_key=$apiKey")
    suspend fun getTvGenres(): Response<ApiResponse>

    @GET("trending/all/week?api_key=$apiKey&include_adult=false")
    suspend fun getTrendingTitles(): Response<ApiResponse>

    @GET("movie/popular?api_key=$apiKey&include_adult=false")
    suspend fun getPopularMovies(): Response<ApiResponse>

    @GET("tv/popular?api_key=$apiKey&include_adult=false")
    suspend fun getPopularTV(): Response<ApiResponse>

    @GET("movie/top_rated?api_key=$apiKey&include_adult=false")
    suspend fun getTopRatedMovies(): Response<ApiResponse>

    @GET("tv/top_rated?api_key=$apiKey&include_adult=false")
    suspend fun getTopRatedTV(): Response<ApiResponse>

    @GET("movie/upcoming?api_key=$apiKey&include_adult=false")
    suspend fun getUpcomingMovies(): Response<ApiResponse>

    /**
     * @param releaseDateFrom Date string in format YYYY-MM-DD
     * @param releaseDateTo Date string in format YYYY-MM-DD
     * @param genresListString pipe or comma separated genre id. E.g. "28|12|53" or "28,15,53".
     * Pipes mean "OR", commas mean "AND"
     */
    @GET("movie/popular/?api_key=$apiKey&include_adult=false")
    suspend fun getPopularMoviesFiltered(
        @Query("page") page: Int,
        @Query("primary_release_date.gte") releaseDateFrom: String = "",
        @Query("primary_release_date.lte") releaseDateTo: String = "",
        @Query("vote_average.gte") scoreFrom: Double = 0.0,
        @Query("vote_average.lte") scoreTo: Double = 10.0,
        @Query("with_genres") genresListString: String = ""
    ): Response<ApiResponse>

    @GET("tv/popular/?api_key=$apiKey&include_adult=false")
    suspend fun getPopularTvFiltered(
        @Query("page") page: Int,
        @Query("first_air_date.gte") releaseDateFrom: String = "",
        @Query("first_air_date.lte") releaseDateTo: String = "",
        @Query("vote_average.gte") scoreFrom: Double = 0.0,
        @Query("vote_average.lte") scoreTo: Double = 10.0,
        @Query("with_genres") genresListString: String = ""
    ): Response<ApiResponse>

    @GET("movie/top_rated/?api_key=$apiKey&include_adult=false")
    suspend fun getTopRatedMoviesFiltered(
        @Query("page") page: Int,
        @Query("primary_release_date.gte") releaseDateFrom: String = "",
        @Query("primary_release_date.lte") releaseDateTo: String = "",
        @Query("vote_average.gte") scoreFrom: Double = 0.0,
        @Query("vote_average.lte") scoreTo: Double = 10.0,
        @Query("with_genres") genresListString: String = ""
    ): Response<ApiResponse>

    @GET("tv/top_rated/?api_key=$apiKey&include_adult=false")
    suspend fun getTopRatedTvFiltered(
        @Query("page") page: Int,
        @Query("first_air_date.gte") releaseDateFrom: String = "",
        @Query("first_air_date.lte") releaseDateTo: String = "",
        @Query("vote_average.gte") scoreFrom: Double = 0.0,
        @Query("vote_average.lte") scoreTo: Double = 10.0,
        @Query("with_genres") genresListString: String = ""
    ): Response<ApiResponse>

    @GET("movie/upcoming/?api_key=$apiKey&include_adult=false")
    suspend fun getUpcomingMoviesFiltered(
        @Query("page") page: Int,
        @Query("primary_release_date.gte") releaseDateFrom: String = "",
        @Query("primary_release_date.lte") releaseDateTo: String = "",
        @Query("vote_average.gte") scoreFrom: Double = 0.0,
        @Query("vote_average.lte") scoreTo: Double = 10.0,
        @Query("with_genres") genresListString: String = ""
    ): Response<ApiResponse>

    @GET("discover/movie/?api_key=$apiKey&include_adult=false")
    suspend fun getDiscoverMoviesFiltered(
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String,
        @Query("primary_release_date.gte") releaseDateFrom: String = "",
        @Query("primary_release_date.lte") releaseDateTo: String = "",
        @Query("vote_average.gte") scoreFrom: Double = 0.0,
        @Query("vote_average.lte") scoreTo: Double = 10.0,
        @Query("with_genres") genresListString: String = ""
    ): Response<ApiResponse>

    @GET("discover/tv/?api_key=$apiKey&include_adult=false")
    suspend fun getDiscoverTvFiltered(
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String,
        @Query("first_air_date.gte") releaseDateFrom: String = "",
        @Query("first_air_date.lte") releaseDateTo: String = "",
        @Query("vote_average.gte") scoreFrom: Double = 0.0,
        @Query("vote_average.lte") scoreTo: Double = 10.0,
        @Query("with_genres") genresListString: String = ""
    ): Response<ApiResponse>

    @GET("movie/{titleId}?api_key=$apiKey&include_adult=false&append_to_response=credits,videos,recommendations,similar")
    suspend fun getMovie(@Path("titleId") titleId: Long): Response<ApiResponse>

    @GET("tv/{titleId}?api_key=$apiKey&include_adult=false&append_to_response=credits,videos,recommendations,similar")
    suspend fun getTv(@Path("titleId") titleId: Long): Response<ApiResponse>

    @GET("configuration?api_key=$apiKey")
    suspend fun getConfiguration(): Response<ApiResponse>
}

fun lol() {
    val x = SortByApiParam.SortTvBy.Popularity_Descending.propertyName
}

/**
 * Holds the values of the "sort_by" parameter of the TMDB Api. These string values can be provided
 * to GET methods of the API that have a "sort_by" query available.
 */
interface SortByApiParam: SortByParameter {
    enum class SortTvBy(override val propertyName: String) : SortByApiParam {
        Popularity_Ascending(propertyName = "popularity.asc"),
        Popularity_Descending(propertyName = "popularity.desc"),
        ReleaseDate_Ascending(propertyName = "first_air_date.asc"),
        ReleaseDate_Descending(propertyName = "first_air_date.desc"),
        Score_Ascending(propertyName = "vote_average.asc"),
        Score_Descending(propertyName = "vote_average.desc")
    }

    enum class SortMoviesBy(override val propertyName: String) : SortByApiParam {
        Popularity_Ascending(propertyName = "popularity.asc"),
        Popularity_Descending(propertyName = "popularity.desc"),
        ReleaseDate_Ascending(propertyName = "primary_release_date.asc"),
        ReleaseDate_Descending(propertyName = "primary_release_date.desc"),
        Revenue_Ascending(propertyName = "revenue.asc"),
        Revenue_Descending(propertyName = "revenue.desc"),
        Title_Ascending(propertyName = "original_title.asc"),
        Title_Descending(propertyName = "original_title.desc"),
        Score_Ascending(propertyName = "vote_average.asc"),
        Score_Descending(propertyName = "vote_average.desc"),
        VoteCount_Ascending(propertyName = "vote_count.asc"),
        VoteCount_Descending(propertyName = "vote_count.desc")
    }
}