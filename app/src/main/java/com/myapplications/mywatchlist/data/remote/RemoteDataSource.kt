package com.myapplications.mywatchlist.data.remote

import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.core.result.ResultOf
import com.myapplications.mywatchlist.data.SearchException
import com.myapplications.mywatchlist.data.mappers.toTitleItems
import com.myapplications.mywatchlist.data.remote.api.SearchApiResponse
import com.myapplications.mywatchlist.data.remote.api.TmdbApi
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface RemoteDataSource {
    /**
     * Searches for the given query in The Movie Database.
     * @return [ResultOf.Success] containing List of [TitleItem] if successful and [ResultOf.Failure]
     * if not.
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItem]
     */
    suspend fun searchTitles(query: String, allGenres: List<Genre>): ResultOf<List<TitleItem>>
}

class RemoteDataSourceImpl(
    private val api: TmdbApi,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : RemoteDataSource {

    override suspend fun searchTitles(
        query: String,
        allGenres: List<Genre>
    ): ResultOf<List<TitleItem>> =
        withContext(dispatcher) {
            val apiResponse = try {
                api.search(query)
            } catch (e: Exception) {
                val error = "Failed to get a response from the api."
                return@withContext ResultOf.Failure(
                    message = error,
                    throwable = SearchException.FailedApiRequestException(error, e)
                )
            }
            val responseBody = apiResponse.body()

            // Checking in case the response was received but body was null or code wasn't 200
            if (apiResponse.code() != 200 || responseBody == null) {
                val error = "Failed to get a response from the api."
                return@withContext ResultOf.Failure(
                    message = error,
                    throwable = SearchException.FailedApiRequestException(error, null)
                )
            }

            // Returning the parsed result
            return@withContext parseSearchTitlesApiResponse(
                responseBody = responseBody,
                allGenres = allGenres
            )
        }

    /**
     * Parses the API's response body to a [ResultOf]
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItem]
     */
    private fun parseSearchTitlesApiResponse(
        responseBody: SearchApiResponse,
        allGenres: List<Genre>
    ): ResultOf<List<TitleItem>> {
        return if (responseBody.titleItems == null) {
            val error = "No titles found for the given search"
            ResultOf.Failure(
                message = error, throwable = SearchException.NothingFoundException(error, null)
            )
        } else {
            ResultOf.Success(data = responseBody.titleItems.toTitleItems(allGenres))
        }
    }

}