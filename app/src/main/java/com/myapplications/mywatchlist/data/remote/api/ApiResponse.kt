package com.myapplications.mywatchlist.data.remote.api

import com.myapplications.mywatchlist.data.entities.MovieApiModel
import com.myapplications.mywatchlist.data.entities.TitleItemApiModel
import com.myapplications.mywatchlist.data.entities.TitleItemMinimalApiModel
import com.myapplications.mywatchlist.data.entities.TvApiModel
import com.myapplications.mywatchlist.domain.entities.Genre

sealed class ApiResponse {

    /**
     * Response class for storing results list of [TitleItemApiModel]
     */
    data class TitlesListResponse(
        val page: Int,
        val titleItems: List<TitleItemApiModel>?,
        val totalPages: Int,
        val totalResults: Int
    ) : ApiResponse()

    /**
     * Response class for storing results list of [TitleItemMinimalApiModel]
     */
    data class TitlesListMinimalResponse(
        val page: Int,
        val titleItems: List<TitleItemMinimalApiModel>?,
        val totalPages: Int,
        val totalResults: Int
    ) : ApiResponse()

    /**
     * Response class for storing results of available genres on TMDB. Can be either TV or Movie
     * list of genres. That's to be determined by the calling Api method.
     */
    data class GenresResponse(val genres: List<Genre>?) : ApiResponse()

    data class MovieResponse(val movie: MovieApiModel?) : ApiResponse()

    data class TvResponse(val tv: TvApiModel?) : ApiResponse()

    data class ConfigurationResponse(
        val baseUrl: String?,
        val backdropSizes: List<String>?,
        val posterSizes: List<String>?,
        val profileSizes: List<String>?
    ) : ApiResponse()
}


