package com.myapplications.mywatchlist.data.repositories

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.core.util.Constants
import com.myapplications.mywatchlist.core.util.NetworkStatusManager
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.data.local.WatchlistDatabase
import com.myapplications.mywatchlist.data.local.titles.TitlesLocalDataSource
import com.myapplications.mywatchlist.data.mappers.toTitleItemFull
import com.myapplications.mywatchlist.data.mediators.TitlesRemoteMediatorProvider
import com.myapplications.mywatchlist.data.remote.TitlesRemoteDataSource
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.entities.TitleListFilter
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.repositories.TitleItemsRepository
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "TITLES_REPOSITORY"

class TitleItemsRepositoryImpl @Inject constructor(
    private val localDataSource: TitlesLocalDataSource,
    private val remoteDataSource: TitlesRemoteDataSource,
    private val genresRepository: GenresRepository,
    private val networkStatusManager: NetworkStatusManager,
    private val database: WatchlistDatabase,
    private val titlesRemoteMediatorProvider: TitlesRemoteMediatorProvider,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : TitleItemsRepository {

    override suspend fun searchAll(query: String, page: Int): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            return@withContext getTitleItemsFullResult(
                requestType = TitleItemsRequestType.SearchAll(query = query, page = page)
            )
        }

    override suspend fun searchMovies(query: String, page: Int): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            return@withContext getTitleItemsFullResult(
                requestType = TitleItemsRequestType.SearchMovies(query = query, page = page)
            )
        }

    override suspend fun searchTV(query: String, page: Int): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            return@withContext getTitleItemsFullResult(
                requestType = TitleItemsRequestType.SearchTV(query = query, page = page)
            )
        }

    override suspend fun bookmarkTitleItem(titleItemFull: TitleItemFull) = withContext(dispatcher) {
        localDataSource.bookmarkTitleItem(titleItemFull = titleItemFull.copy(isWatchlisted = true))
    }

    override suspend fun unBookmarkTitleItem(titleItemFull: TitleItemFull) = withContext(dispatcher) {
        localDataSource.unBookmarkTitleItem(titleItemFull = titleItemFull)
    }

    override suspend fun getWatchlistedTitles(): List<TitleItemFull>? = withContext(dispatcher) {
        localDataSource.getAllBookmarkedTitles()
    }

    override fun allWatchlistedTitleItems(): Flow<List<TitleItemFull>> {
        return localDataSource.allWatchlistedTitlesFlow()
    }

    override suspend fun getTrendingTitles(): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.TrendingMoviesAndTV
        )
    }

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun getPopularMovies(
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {

        // TODO: Move to separate method
        val genresList = genresRepository.getAvailableGenres()
        val titlesFlow = Pager(
            config = getPagingConfig(),
            pagingSourceFactory = { localDataSource.getCachedPopularMovies() },
            remoteMediator = titlesRemoteMediatorProvider.getPopularMoviesRemoteMediator(
                filter = filter,
                genres = genresList
            )
        ).flow.map { pagingData ->
            pagingData.map { it.toTitleItemFull() }
        }

        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.PopularMovies(page = page, filter = filter)
        )
    }

    override suspend fun getPopularTV(
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.PopularTV(page = page, filter = filter)
        )
    }

    override suspend fun getTopRatedMovies(
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.TopRatedMovies(page = page, filter = filter)
        )
    }

    override suspend fun getTopRatedTV(
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.TopRatedTV(page = page, filter = filter)
        )
    }

    override suspend fun getUpcomingMovies(
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.UpcomingMovies(page = page, filter = filter)
        )
    }

    override suspend fun getDiscoverMovies(
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.DiscoverMovies(page = page, filter = filter)
        )
    }

    override suspend fun getDiscoverTV(
        page: Int,
        filter: TitleListFilter
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.DiscoverTV(page = page, filter = filter)
        )
    }

    /**
     * General function to get a result from remote data source
     * @param requestType of type [TitleItemsRequestType] determines what type of request will be made
     */
    private suspend fun getTitleItemsFullResult(
        requestType: TitleItemsRequestType
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        if (!networkStatusManager.isOnline()){
            return@withContext ResultOf.Failure(
                message = null,
                throwable = ApiGetTitleItemsExceptions.NoConnectionException(null, null)
            )
        }
        val genresList = genresRepository.getAvailableGenres()

        val result = when (requestType) {
            TitleItemsRequestType.TrendingMoviesAndTV ->
                remoteDataSource.getTrendingTitles(allGenres = genresList)
            is TitleItemsRequestType.SearchAll ->
                remoteDataSource.searchAllTitles(
                    allGenres = genresList,
                    page = requestType.page,
                    query = requestType.query
                )
            is TitleItemsRequestType.SearchMovies ->
                remoteDataSource.searchMovies(
                    allGenres = genresList,
                    page = requestType.page,
                    query = requestType.query
                )
            is TitleItemsRequestType.SearchTV ->
                remoteDataSource.searchTV(
                    allGenres = genresList,
                    page = requestType.page,
                    query = requestType.query
                )
            is TitleItemsRequestType.DiscoverMovies ->
                remoteDataSource.getDiscoverMovies(
                    allGenres = genresList,
                    page = requestType.page,
                    filter = requestType.filter
                )
            is TitleItemsRequestType.DiscoverTV ->
                remoteDataSource.getDiscoverTV(
                    allGenres = genresList,
                    page = requestType.page,
                    filter = requestType.filter
                )
            is TitleItemsRequestType.PopularMovies ->
                remoteDataSource.getPopularMovies(
                    allGenres = genresList,
                    page = requestType.page,
                    filter = requestType.filter
                )
            is TitleItemsRequestType.PopularTV ->
                remoteDataSource.getPopularTV(
                    allGenres = genresList,
                    page = requestType.page,
                    filter = requestType.filter
                )
            is TitleItemsRequestType.TopRatedMovies ->
                remoteDataSource.getTopRatedMovies(
                    allGenres = genresList,
                    page = requestType.page,
                    filter = requestType.filter
                )
            is TitleItemsRequestType.TopRatedTV ->
                remoteDataSource.getTopRatedTV(
                    allGenres = genresList,
                    page = requestType.page,
                    filter = requestType.filter
                )
            is TitleItemsRequestType.UpcomingMovies ->
                remoteDataSource.getUpcomingMovies(
                    allGenres = genresList,
                    page = requestType.page,
                    filter = requestType.filter
                )
        }
        return@withContext parseTitlesListResult(result)
    }

    /**
     * Parses the result fetched from Remote Data Source: if result is Success, then filters the
     * received list by exchanging titles with those in the local database (if those titles are the
     * same). This is so UI lists can show whether title items are marked as Watchlisted or not.
     * @param result is a [ResultOf] returned from remote data source.
     * @return [ResultOf] that can be further returned to the requester.
     */
    private suspend fun parseTitlesListResult(
        result: ResultOf<List<TitleItemFull>>
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        when (result) {
            is ResultOf.Failure -> return@withContext result
            is ResultOf.Success -> {
                val titlesFilteredForWatchlisted = mutableListOf<TitleItemFull>()
                result.data.forEach { titleItem ->
                    val isWatchlisted = localDataSource.checkIfTitleItemWatchlisted(titleItem)
                    if (isWatchlisted) {
                        titlesFilteredForWatchlisted.add(titleItem.copy(isWatchlisted = true))
                    } else {
                        titlesFilteredForWatchlisted.add(titleItem)
                    }
                }
                return@withContext ResultOf.Success(data = titlesFilteredForWatchlisted)
            }
        }
    }

    private fun getPagingConfig(
        pageSize: Int = Constants.PAGE_SIZE,
        prefetchDistance: Int = 2 * Constants.PAGE_SIZE,
        enablePlaceholders: Boolean = true,
        initialLoadSize: Int = 2 * Constants.PAGE_SIZE,
        maxSize: Int = 200,
    ): PagingConfig = PagingConfig(
        pageSize = pageSize,
        prefetchDistance = prefetchDistance,
        enablePlaceholders = enablePlaceholders,
        initialLoadSize = initialLoadSize,
        maxSize = maxSize
    )

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