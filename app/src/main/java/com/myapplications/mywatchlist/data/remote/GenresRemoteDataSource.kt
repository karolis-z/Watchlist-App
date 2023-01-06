package com.myapplications.mywatchlist.data.remote

import android.util.Log
import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.GetGenresExceptions
import com.myapplications.mywatchlist.data.remote.api.ApiResponse
import com.myapplications.mywatchlist.data.remote.api.TmdbApi
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface GenresRemoteDataSource {
    /**
     * Retrieves two lists of available Genre, combines them and returns a unique list of them in
     * [ApiResponse.GenresResponse]
     * @return [ResultOf.Success] with [ApiResponse.GenresResponse] if successful or [ResultOf.Failure]
     * if not.
     */
    suspend fun getAllGenresFromApi(): ResultOf<ApiResponse.GenresResponse>
}

private const val TAG = "GENRES_REMOTE_SOURCE"

class GenresRemoteDataSourceImpl @Inject constructor(
    private val api: TmdbApi,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : GenresRemoteDataSource {

    override suspend fun getAllGenresFromApi(): ResultOf<ApiResponse.GenresResponse> =
        withContext(dispatcher) {

            val tvGenresApiResponse = try {
                api.getTvGenres()
            } catch (e: Exception) {
                val error = "Could not get a response with TV Genres list from the API."
                Log.e(TAG, "getAllGenresFromApi: $error", e)
                null
            }

            val movieGenresApiResponse = try {
                api.getMovieGenres()
            } catch (e: Exception) {
                val error = "Could not get a response with Movie Genres list from the API."
                Log.e(TAG, "getAllGenresFromApi: $error", e)
                null
            }

            // If either of the api calls was unsuccessful, returning Failure.
            if (movieGenresApiResponse == null
                || tvGenresApiResponse == null
                || movieGenresApiResponse.code() != 200
                || tvGenresApiResponse.code() != 200
            ) {
                return@withContext ResultOf.Failure(
                    message = "Could not either TV or Movie Genre List",
                    throwable = GetGenresExceptions.FailedApiRequestException(null, null)
                )
            }

            // If response bodies are null or lists of Genre are null - returning Failure
            val tvGenresResponseBody = try {
                tvGenresApiResponse.body() as ApiResponse.GenresResponse?
            } catch (e: Exception) {
                null // Exception and null check will be handled below
            }
            val movieGenresResponseBody = try {
                movieGenresApiResponse.body() as ApiResponse.GenresResponse?
            } catch (e: Exception) {
                null // Exception and null check will be handled below
            }

            if (
                tvGenresResponseBody == null
                || movieGenresResponseBody == null
                || tvGenresResponseBody.genres == null
                || movieGenresResponseBody.genres == null
            ) {
                return@withContext ResultOf.Failure(
                    message = "Genre requests from api were successful but bodies were null",
                    throwable = GetGenresExceptions.NothingFoundException(null, null)
                )
            }

            // If all successful - combine lists (use only unique genre ids) and return them
            val combinedList =
                (tvGenresResponseBody.genres + movieGenresResponseBody.genres).distinctBy { it.id }

            return@withContext ResultOf.Success(data = ApiResponse.GenresResponse(combinedList))
        }

}