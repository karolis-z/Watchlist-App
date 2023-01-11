package com.myapplications.mywatchlist.data.remote.api

import com.myapplications.mywatchlist.data.entities.MovieApiModel
import com.myapplications.mywatchlist.data.entities.TitleItemApiModel
import com.myapplications.mywatchlist.domain.entities.Genre

sealed class ApiResponse {

    /**
     * Response class for storing results of a query to the TMDB
     */
    data class TitlesListResponse(
        val page: Int,
        val titleItems: List<TitleItemApiModel>?,
        val totalPages: Int,
        val totalResults: Int
    ) : ApiResponse()

    /**
     * Response class for storing results of available genres on TMDB. Can be either TV or Movie
     * list of genres. That's to be determined by the calling Api method.
     */
    data class GenresResponse(
        val genres: List<Genre>?
    ) : ApiResponse()

    data class MovieResponse(
        val movie: MovieApiModel?
    ) : ApiResponse()

//    data class TvResponse(
//        val tv: TvApiModel
//    ) : ApiResponse()
}


