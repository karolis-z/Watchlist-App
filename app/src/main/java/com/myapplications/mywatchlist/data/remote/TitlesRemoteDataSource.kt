package com.myapplications.mywatchlist.data.remote

import android.util.Log
import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.data.datastore.UserPrefsRepository
import com.myapplications.mywatchlist.data.mappers.toTitleItemsFull
import com.myapplications.mywatchlist.data.remote.api.ApiResponse
import com.myapplications.mywatchlist.data.remote.api.TmdbApi
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

interface TitlesRemoteDataSource {
    /**
     * Searches for the given query in The Movie Database.
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * if not.
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItemFull]
     */
    suspend fun searchTitles(query: String, allGenres: List<Genre>): ResultOf<List<TitleItemFull>>

    /**
     * Retrieves the titles that are trending this week from TMDB.
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * if not.
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItemFull]
     */
    suspend fun getTrendingTitles(allGenres: List<Genre>): ResultOf<List<TitleItemFull>>

    suspend fun getTrendingTitlesPaginated(allGenres: List<Genre>, page: Int): ResultOf<List<TitleItemFull>>

    /**
     * Retrieves titles that are popular today from TMDB
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItemFull]
     */
    suspend fun getPopularTitles(allGenres: List<Genre>): ResultOf<List<TitleItemFull>>

    /**
     * Retrieves titles that are top rated from TMDB
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItemFull]
     */
    suspend fun getTopRatedTitles(allGenres: List<Genre>): ResultOf<List<TitleItemFull>>

    /**
     * Retrieves titles that are upcoming movies from TMDB
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItemFull]
     */
    suspend fun getUpcomingMovies(allGenres: List<Genre>): ResultOf<List<TitleItemFull>>
}

private const val TAG = "TITLES_REMOTE_DATASRC"

class TitlesRemoteDataSourceImpl @Inject constructor(
    private val api: TmdbApi,
    private val userPrefsRepository: UserPrefsRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : TitlesRemoteDataSource {

    override suspend fun searchTitles(
        query: String,
        allGenres: List<Genre>
    ): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            return@withContext getTitleItemsFullResult(
                requestType = TitleItemsRequestType.SearchQuery(query),
                allGenres = allGenres
            )
        }

    override suspend fun getTrendingTitles(allGenres: List<Genre>): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            return@withContext getTitleItemsFullResult(
                requestType = TitleItemsRequestType.TrendingMoviesAndTV,
                allGenres = allGenres
            )
        }

    override suspend fun getTrendingTitlesPaginated(
        allGenres: List<Genre>,
        page: Int
    ): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            return@withContext getTitleItemsFullResult(
                requestType = TitleItemsRequestType.TrendingMoviesAndTVPaginated(page = page),
                allGenres = allGenres
            )
        }

    override suspend fun getPopularTitles(allGenres: List<Genre>): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            return@withContext getTitleItemsFullResult(
                requestType = TitleItemsRequestType.PopularMoviesAndTV,
                allGenres = allGenres
            )
        }

    override suspend fun getTopRatedTitles(allGenres: List<Genre>): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            return@withContext getTitleItemsFullResult(
                requestType = TitleItemsRequestType.TopRatedMoviesAndTV,
                allGenres = allGenres
            )
        }

    override suspend fun getUpcomingMovies(allGenres: List<Genre>): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            return@withContext getTitleItemsFullResult(
                requestType = TitleItemsRequestType.UpcomingMovies,
                allGenres = allGenres
            )
        }

    private suspend fun getTitleItemsFullResult(
        requestType: TitleItemsRequestType,
        allGenres: List<Genre>
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        val apiResponses = mutableListOf<Response<ApiResponse>>()
        try {
            when (requestType) {
                TitleItemsRequestType.TrendingMoviesAndTV ->
                    apiResponses.add(api.getTrendingTitles())
                TitleItemsRequestType.PopularMoviesAndTV -> {
                    apiResponses.add(api.getPopularMovies())
                    apiResponses.add(api.getPopularTV())
                }
                is TitleItemsRequestType.SearchQuery ->
                    apiResponses.add(api.search(requestType.query))
                TitleItemsRequestType.TopRatedMoviesAndTV -> {
                    apiResponses.add(api.getTopRatedTV())
                    apiResponses.add(api.getTopRatedMovies())
                }
                TitleItemsRequestType.UpcomingMovies ->
                    apiResponses.add(api.getUpcomingMovies())
                is TitleItemsRequestType.TrendingMoviesAndTVPaginated ->
                    apiResponses.add(api.getTrendingTitlesPaging(page = requestType.page))
            }
        } catch (e: Exception) {
            return@withContext getFailedApiResponseResult(exception = e)
        }

        val responseBodies = try {
            apiResponses.map { it.body() as ApiResponse.TitlesListResponse? }
        } catch (e: Exception) {
            return@withContext getFailedApiResponseResult(exception = e)
        }

        // Checking in case the response was received but body was null or code wasn't 200
        for (i in apiResponses.indices) {
            if (apiResponses[i].code() != 200 || responseBodies[i] == null) {
                return@withContext getFailedApiResponseResult(exception = null)
            }
        }

        // Returning the parsed result
        val parsedResult = parseTitleItemsFullApiResponse(
            /* Filtering not null to not have an error, but we already checked for null in the
            loop above and if the method reached this point, all responseBodies will be non-null */
            responseBodies = responseBodies.filterNotNull(),
            allGenres = allGenres
        )
        return@withContext when (parsedResult) {
            is ResultOf.Failure -> parsedResult
            is ResultOf.Success -> {
                when (requestType) {
                    TitleItemsRequestType.PopularMoviesAndTV -> parsedResult.copy(
                        data = parsedResult.data.sortedByDescending { it.popularity }
                    )
                    /* Sorting top rated by voteAverage but also by vote count to list titles higher
                    with same vote average but higher vote count */
                    TitleItemsRequestType.TopRatedMoviesAndTV -> parsedResult.copy(
                        data = parsedResult.data.sortedWith(
                            compareBy(
                                { -it.voteAverage },
                                { -it.voteCount })
                        )
                    )
                    is TitleItemsRequestType.SearchQuery,
                    TitleItemsRequestType.TrendingMoviesAndTV,
                    is TitleItemsRequestType.TrendingMoviesAndTVPaginated,
                    TitleItemsRequestType.UpcomingMovies -> parsedResult.copy(
                        data = parsedResult.data.sortedBy { it.releaseDate }
                    )
                }
            }
        }
    }

    /**
     * Parses the API's response bodies to a [ResultOf]. The methods accepts a list, but it can have
     * only 1 [Response] or multiple. If multiple - method will return a merged [ResultOf].
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItemFull]
     * @param responseBodies is a list [ApiResponse.TitlesListResponse]
     */
    private suspend fun parseTitleItemsFullApiResponse(
        responseBodies: List<ApiResponse.TitlesListResponse>,
        allGenres: List<Genre>
    ): ResultOf<List<TitleItemFull>> {
        val titleItems = mutableListOf<TitleItemFull>()
        val apiConfiguration = userPrefsRepository.getApiConfiguration()
        responseBodies.forEach {
            if (it.titleItems == null) {
                val error = "No titles found for the request."
                return ResultOf.Failure(
                    message = error,
                    throwable = ApiGetTitleItemsExceptions.NothingFoundException(error, null)
                )
            } else {
                titleItems.addAll(
                    it.titleItems.toTitleItemsFull(
                        allGenres = allGenres,
                        apiConfiguration = apiConfiguration
                    )
                )
            }
        }
        return if (titleItems.isNotEmpty()) {
            ResultOf.Success(data = titleItems)
        } else {
            val error = "No titles found for the request."
            ResultOf.Failure(
                message = error,
                throwable = ApiGetTitleItemsExceptions.NothingFoundException(error, null)
            )
        }
    }

    private fun getFailedApiResponseResult(exception: Exception?): ResultOf.Failure {
        val error = "Failed to get a response from the api."
        Log.e(TAG, "getFailedApiResponseResult: $exception", exception)
        return ResultOf.Failure(
            message = error,
            throwable = ApiGetTitleItemsExceptions.FailedApiRequestException(error, exception)
        )
    }

    private sealed class TitleItemsRequestType {
        object TrendingMoviesAndTV : TitleItemsRequestType()
        data class TrendingMoviesAndTVPaginated(val page: Int) : TitleItemsRequestType()
        object PopularMoviesAndTV : TitleItemsRequestType()
        object UpcomingMovies : TitleItemsRequestType()
        object TopRatedMoviesAndTV : TitleItemsRequestType()
        data class SearchQuery(val query: String): TitleItemsRequestType()
    }
}