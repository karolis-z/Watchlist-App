package com.myapplications.mywatchlist.data.remote

import android.util.Log
import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.ApiGetDetailsException
import com.myapplications.mywatchlist.data.datastore.UserPrefsRepository
import com.myapplications.mywatchlist.data.mappers.toMovie
import com.myapplications.mywatchlist.data.mappers.toTv
import com.myapplications.mywatchlist.data.remote.api.ApiResponse
import com.myapplications.mywatchlist.data.remote.api.TmdbApi
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.Title
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface RemoteDetailsDataSource {

    /**
     * Fetches a [Title] of [TitleType] from the Api within a [ResultOf] .
     * @return [ResultOf.Success] containing a [Title] or [ResultOf.Failure] if unsuccessful.
     * @param mediaId is the is of the TV or Movie object in the Api.
     */
    suspend fun getTitle(mediaId: Long, type: TitleType, allGenres: List<Genre>): ResultOf<Title>
}

private const val TAG = "REMOTE_DETAILS_DATASRC"

class RemoteDetailsDataSourceImpl @Inject constructor(
    private val api: TmdbApi,
    private val userPrefsRepository: UserPrefsRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : RemoteDetailsDataSource {

    override suspend fun getTitle(
        mediaId: Long,
        type: TitleType,
        allGenres: List<Genre>
    ): ResultOf<Title> = withContext(dispatcher) {
        val apiResponse = try {
            when(type) {
                TitleType.MOVIE -> api.getMovie(mediaId)
                TitleType.TV -> api.getTv(mediaId)
            }
        } catch (e: Exception) {
            return@withContext getFailedApiResponseResult(exception = e)
        }

        val responseBody = try {
            when(type) {
                TitleType.MOVIE -> apiResponse.body() as ApiResponse.MovieResponse?
                TitleType.TV -> apiResponse.body() as ApiResponse.TvResponse?
            }
        } catch (e: Exception) {
            null // Exception and null check will be handled below
        }

        // Checking in case the response was received but body was null or code wasn't 200
        if (apiResponse.code() != 200 || responseBody == null) {
            return@withContext getFailedApiResponseResult(exception = null)
        }

        // Returning the parsed result
        return@withContext parseTitleResponse(
            responseBody = responseBody, type = type, allGenres = allGenres
        )
    }

    /**
     * Parses the API's response body to a [ResultOf]
     * @param type required to identify which [TitleType] has been returned from the api
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [Title]
     */
    private suspend fun parseTitleResponse(responseBody: ApiResponse, type: TitleType, allGenres: List<Genre>): ResultOf<Title> {
        return when(type) {
            TitleType.MOVIE -> {
                val responseMovieBody = responseBody as ApiResponse.MovieResponse
                if (responseMovieBody.movie == null) {
                    val error = "No movie found."
                    ResultOf.Failure(
                        message = error,
                        throwable = ApiGetDetailsException.NothingFoundException(error, null)
                    )
                } else {
                    val apiConfiguration = userPrefsRepository.getApiConfiguration()
                    ResultOf.Success(data = responseMovieBody.movie.toMovie(
                        allGenres = allGenres,
                        apiConfiguration = apiConfiguration
                    ))
                }
            }
            TitleType.TV -> {
                val responseTvBody = responseBody as ApiResponse.TvResponse
                if (responseTvBody.tv == null) {
                    val error = "No tv found."
                    ResultOf.Failure(
                        message = error,
                        throwable = ApiGetDetailsException.NothingFoundException(error, null)
                    )
                } else {
                    val apiConfiguration = userPrefsRepository.getApiConfiguration()
                    ResultOf.Success(data = responseTvBody.tv.toTv(
                        allGenres = allGenres,
                        apiConfiguration = apiConfiguration
                    ))
                }
            }
        }
    }

    private fun getFailedApiResponseResult(exception: Exception?): ResultOf.Failure {
        val error = "Failed to get a Details response from the api."
        Log.e(TAG, "getFailedApiResponseResult: $exception", exception)
        return ResultOf.Failure(
            message = error,
            throwable = ApiGetDetailsException.FailedApiRequestException(error, exception)
        )
    }
}