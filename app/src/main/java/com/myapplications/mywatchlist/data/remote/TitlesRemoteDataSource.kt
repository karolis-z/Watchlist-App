package com.myapplications.mywatchlist.data.remote

import android.util.Log
import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.data.datastore.UserPrefsRepository
import com.myapplications.mywatchlist.data.mappers.toTitleItemsFull
import com.myapplications.mywatchlist.data.remote.api.ApiResponse
import com.myapplications.mywatchlist.data.remote.api.SortByApiParam
import com.myapplications.mywatchlist.data.remote.api.TmdbApi
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.entities.TitleListFilter
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
     * @param page page number of the Api's results
     */
    suspend fun searchAllTitles(
        query: String,
        allGenres: List<Genre>,
        page: Int
    ): ResultOf<List<TitleItemFull>>

    /**
     * Searches for the given query among Movies in The Movie Database.
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * if not.
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItemFull]
     * @param page page number of the Api's results
     */
    suspend fun searchMovies(
        query: String,
        allGenres: List<Genre>,
        page: Int
    ): ResultOf<List<TitleItemFull>>

    /**
     * Searches for the given query among TV Shows in The Movie Database.
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * if not.
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItemFull]
     * @param page page number of the Api's results
     */
    suspend fun searchTV(
        query: String,
        allGenres: List<Genre>,
        page: Int
    ): ResultOf<List<TitleItemFull>>

    /**
     * Retrieves the titles that are trending this week from TMDB.
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * if not.
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItemFull]
     */
    suspend fun getTrendingTitles(allGenres: List<Genre>): ResultOf<List<TitleItemFull>>

    /**
     * Retrieves Movies that are popular today from TMDB
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItemFull]
     * @param page page number of the Api's results
     */
    suspend fun getPopularMovies(
        allGenres: List<Genre>,
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>>

    /**
     * Retrieves TV Shows that are popular today from TMDB
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItemFull]
     * @param page page number of the Api's results
     */
    suspend fun getPopularTV(
        allGenres: List<Genre>,
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>>

    /**
     * Retrieves Movies that are top rated from TMDB
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItemFull]
     * @param page page number of the Api's results
     */
    suspend fun getTopRatedMovies(
        allGenres: List<Genre>,
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>>

    /**
     * Retrieves TV Shows that are top rated from TMDB
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItemFull]
     * @param page page number of the Api's results
     */
    suspend fun getTopRatedTV(
        allGenres: List<Genre>,
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>>

    /**
     * Retrieves upcoming movies from TMDB sorted by popularity
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItemFull]
     * @param page page number of the Api's results
     */
    suspend fun getUpcomingMovies(
        allGenres: List<Genre>,
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>>

    /**
     * Retrieves custom filtered Movies sorted by popularity from TMDB
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItemFull]
     * @param page page number of the Api's results
     */
    suspend fun getDiscoverMovies(
        allGenres: List<Genre>,
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>>

    /**
     * Retrieves custom filtered TV Shows sorted by popularity from TMDB
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * @param allGenres required to map genre ids received from API to to a list of [Genre] used in
     * [TitleItemFull]
     * @param page page number of the Api's results
     */
    suspend fun getDiscoverTV(
        allGenres: List<Genre>,
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>>
}

private const val TAG = "TITLES_REMOTE_DATASRC"

class TitlesRemoteDataSourceImpl @Inject constructor(
    private val api: TmdbApi,
    private val userPrefsRepository: UserPrefsRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : TitlesRemoteDataSource {

    override suspend fun searchAllTitles(
        query: String,
        allGenres: List<Genre>,
        page: Int
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.SearchAll(query = query, page = page),
            allGenres = allGenres
        )
    }

    override suspend fun searchMovies(
        query: String,
        allGenres: List<Genre>,
        page: Int
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.SearchMovies(query = query, page = page),
            allGenres = allGenres
        )
    }

    override suspend fun searchTV(
        query: String,
        allGenres: List<Genre>,
        page: Int
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.SearchTV(query = query, page = page),
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

    override suspend fun getPopularMovies(
        allGenres: List<Genre>,
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.PopularMovies(page = page, filter = filter),
            allGenres = allGenres
        )
    }

    override suspend fun getPopularTV(
        allGenres: List<Genre>,
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.PopularTV(page = page, filter = filter),
            allGenres = allGenres
        )
    }

    override suspend fun getTopRatedMovies(
        allGenres: List<Genre>,
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.TopRatedMovies(page = page, filter = filter),
            allGenres = allGenres
        )
    }

    override suspend fun getTopRatedTV(
        allGenres: List<Genre>,
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.TopRatedTV(page = page, filter = filter),
            allGenres = allGenres
        )
    }

    override suspend fun getUpcomingMovies(
        allGenres: List<Genre>,
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.UpcomingMovies(page = page, filter = filter),
            allGenres = allGenres
        )
    }

    override suspend fun getDiscoverMovies(
        allGenres: List<Genre>,
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.DiscoverMovies(page = page, filter = filter),
            allGenres = allGenres
        )
    }

    override suspend fun getDiscoverTV(
        allGenres: List<Genre>,
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.DiscoverTV(page = page, filter = filter),
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
                is TitleItemsRequestType.DiscoverMovies ->
                    apiResponses.add(
                        api.getDiscoverMoviesFiltered(
                            page = requestType.page,
                            releaseDateFrom = requestType.filter.releaseDateFrom?.toString() ?: "",
                            releaseDateTo = requestType.filter.releaseDateTo?.toString() ?: "",
                            scoreFrom = requestType.filter.scoreFrom,
                            scoreTo = requestType.filter.scoreTo,
                            genresListString = getGenresListString(requestType.filter.withGenres),
                            sortBy = requestType.filter.sortBy?.propertyName
                                ?: SortByApiParam.SortMoviesBy.Popularity_Descending.propertyName
                        )
                    )
                is TitleItemsRequestType.DiscoverTV ->
                    apiResponses.add(
                        api.getDiscoverTvFiltered(
                            page = requestType.page,
                            releaseDateFrom = requestType.filter.releaseDateFrom?.toString() ?: "",
                            releaseDateTo = requestType.filter.releaseDateTo?.toString() ?: "",
                            scoreFrom = requestType.filter.scoreFrom,
                            scoreTo = requestType.filter.scoreTo,
                            genresListString = getGenresListString(requestType.filter.withGenres),
                            sortBy = requestType.filter.sortBy?.propertyName
                                ?: SortByApiParam.SortTvBy.Popularity_Descending.propertyName
                        )
                    )
                is TitleItemsRequestType.PopularMovies ->
                    apiResponses.add(
                        api.getPopularMoviesFiltered(
                            page = requestType.page,
                            releaseDateFrom = requestType.filter.releaseDateFrom?.toString() ?: "",
                            releaseDateTo = requestType.filter.releaseDateTo?.toString() ?: "",
                            scoreFrom = requestType.filter.scoreFrom,
                            scoreTo = requestType.filter.scoreTo,
                            genresListString = getGenresListString(requestType.filter.withGenres)
                        )
                    )
                is TitleItemsRequestType.PopularTV ->
                    apiResponses.add(
                        api.getPopularTvFiltered(
                            page = requestType.page,
                            releaseDateFrom = requestType.filter.releaseDateFrom?.toString() ?: "",
                            releaseDateTo = requestType.filter.releaseDateTo?.toString() ?: "",
                            scoreFrom = requestType.filter.scoreFrom,
                            scoreTo = requestType.filter.scoreTo,
                            genresListString = getGenresListString(requestType.filter.withGenres)
                        )
                    )
                is TitleItemsRequestType.TopRatedMovies ->
                    apiResponses.add(
                        api.getTopRatedMoviesFiltered(
                            page = requestType.page,
                            releaseDateFrom = requestType.filter.releaseDateFrom?.toString() ?: "",
                            releaseDateTo = requestType.filter.releaseDateTo?.toString() ?: "",
                            scoreFrom = requestType.filter.scoreFrom,
                            scoreTo = requestType.filter.scoreTo,
                            genresListString = getGenresListString(requestType.filter.withGenres)
                        )
                    )
                is TitleItemsRequestType.TopRatedTV ->
                    apiResponses.add(
                        api.getTopRatedTvFiltered(
                            page = requestType.page,
                            releaseDateFrom = requestType.filter.releaseDateFrom?.toString() ?: "",
                            releaseDateTo = requestType.filter.releaseDateTo?.toString() ?: "",
                            scoreFrom = requestType.filter.scoreFrom,
                            scoreTo = requestType.filter.scoreTo,
                            genresListString = getGenresListString(requestType.filter.withGenres)
                        )
                    )
                is TitleItemsRequestType.UpcomingMovies ->
                    apiResponses.add(
                        api.getUpcomingMoviesFiltered(
                            page = requestType.page,
                            releaseDateFrom = requestType.filter.releaseDateFrom?.toString() ?: "",
                            releaseDateTo = requestType.filter.releaseDateTo?.toString() ?: "",
                            scoreFrom = requestType.filter.scoreFrom,
                            scoreTo = requestType.filter.scoreTo,
                            genresListString = getGenresListString(requestType.filter.withGenres)
                        )
                    )
                is TitleItemsRequestType.SearchAll ->
                    apiResponses.add(
                        api.searchAll(query = requestType.query, page = requestType.page)
                    )
                is TitleItemsRequestType.SearchMovies ->
                    apiResponses.add(
                        api.searchMovies(query = requestType.query, page = requestType.page)
                    )
                is TitleItemsRequestType.SearchTV ->
                    apiResponses.add(
                        api.searchTV(query = requestType.query, page = requestType.page)
                    )
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
                if (requestType is TitleItemsRequestType.TopRatedMovies || requestType is TitleItemsRequestType.TopRatedTV) {
                    /* Sorting top rated by voteAverage but also by vote count to list titles higher
                    with same vote average but higher vote count */
                    parsedResult.copy(
                        data = parsedResult.data.sortedWith(
                            compareBy({ -it.voteAverage }, { -it.voteCount })
                        )
                    )
                } else {
                    parsedResult
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

    private fun getGenresListString(genres: List<Genre>): String {
        return if (genres.isEmpty()) {
            ""
        } else {
            genres.map { it.id }.joinToString(separator = "|")
        }
    }

    private sealed class TitleItemsRequestType {
        object TrendingMoviesAndTV : TitleItemsRequestType()
        data class PopularMovies(val page: Int, val filter: TitleListFilter) :
            TitleItemsRequestType()
        data class PopularTV(val page: Int, val filter: TitleListFilter) : TitleItemsRequestType()
        data class UpcomingMovies(val page: Int, val filter: TitleListFilter) :
            TitleItemsRequestType()
        data class TopRatedMovies(val page: Int, val filter: TitleListFilter) :
            TitleItemsRequestType()
        data class TopRatedTV(val page: Int, val filter: TitleListFilter) : TitleItemsRequestType()
        data class SearchAll(val query: String, val page: Int) : TitleItemsRequestType()
        data class SearchMovies(val query: String, val page: Int) : TitleItemsRequestType()
        data class SearchTV(val query: String, val page: Int) : TitleItemsRequestType()
        data class DiscoverMovies(val page: Int, val filter: TitleListFilter) :
            TitleItemsRequestType()
        data class DiscoverTV(val page: Int, val filter: TitleListFilter) : TitleItemsRequestType()
    }
}