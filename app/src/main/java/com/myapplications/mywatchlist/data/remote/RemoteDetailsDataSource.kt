package com.myapplications.mywatchlist.data.remote

import android.util.Log
import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.ApiGetDetailsException
import com.myapplications.mywatchlist.data.mappers.toMovie
import com.myapplications.mywatchlist.data.remote.api.ApiResponse
import com.myapplications.mywatchlist.data.remote.api.TmdbApi
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.Movie
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface RemoteDetailsDataSource {
    suspend fun getMovie(id: Long, allGenres: List<Genre>): ResultOf<Movie>
}

private const val TAG = "REMOTE_DETAILS_DATASRC"

class RemoteDetailsDataSourceImpl @Inject constructor(
    private val api: TmdbApi,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : RemoteDetailsDataSource {

    override suspend fun getMovie(id: Long, allGenres: List<Genre>): ResultOf<Movie> =
        withContext(dispatcher) {
            val apiResponse = try {
                api.getMovie(id)
            } catch (e: Exception) {
                return@withContext getFailedApiResponseResult(exception = e)
            }

            val responseBody = try {
                apiResponse.body() as ApiResponse.MovieResponse?
            } catch (e: Exception) {
                null // Exception and null check will be handled below
            }

            // Checking in case the response was received but body was null or code wasn't 200
            if (apiResponse.code() != 200 || responseBody == null) {
                return@withContext getFailedApiResponseResult(exception = null)
            }

            // Returning the parsed result
            return@withContext parseMovieApiResponse(
                responseBody = responseBody, allGenres = allGenres
            )
        }

    /**
     * Parses the API's response body to a [ResultOf]
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [Movie]
     */
    private fun parseMovieApiResponse(
        responseBody: ApiResponse.MovieResponse,
        allGenres: List<Genre>
    ): ResultOf<Movie> {
        return if (responseBody.movie == null) {
            /* This shouldn't really happen because we rely on existing ids but in rare scenarios
            * this might happen if ids change in the api's backend */
            val error = "No movie found."
            ResultOf.Failure(
                message = error,
                throwable = ApiGetDetailsException.NothingFoundException(error, null)
            )
        } else {
            ResultOf.Success(data = responseBody.movie.toMovie(allGenres))
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