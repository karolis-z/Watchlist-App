package com.myapplications.mywatchlist.data.repositories

import androidx.paging.*
import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.core.util.Constants
import com.myapplications.mywatchlist.core.util.NetworkStatusManager
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.data.local.titles.TitlesLocalDataSource
import com.myapplications.mywatchlist.data.mappers.toTitleItemFull
import com.myapplications.mywatchlist.data.mediators.TitlesRemoteMediatorProvider
import com.myapplications.mywatchlist.data.remote.TitlesRemoteDataSource
import com.myapplications.mywatchlist.domain.entities.RecentSearch
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
    private val titlesRemoteMediatorProvider: TitlesRemoteMediatorProvider,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : TitleItemsRepository {

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

    override suspend fun getTrendingTitles(): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            getTitleItemsFullResult(requestType = TitleItemsRequestType.TrendingMoviesAndTV)
        }

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun getDiscoverMoviesPaginated(
        filter: TitleListFilter
    ): Flow<PagingData<TitleItemFull>> = withContext(dispatcher) {
        // TODO: implement a better way to get genres, perhaps getting them in init block and
        //  then reusing in all other functions
        val genresList = genresRepository.getAvailableGenres()
        return@withContext Pager(
            config = getPagingConfig(),
            pagingSourceFactory = { localDataSource.getCachedDiscoverMovies() },
            remoteMediator = titlesRemoteMediatorProvider.getDiscoverMovieRemoteMediator(
                filter = filter,
                genres = genresList
            )
        ).flow.map { pagingData ->
            pagingData.map { it.toTitleItemFull() }
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun getDiscoverTVPaginated(
        filter: TitleListFilter
    ): Flow<PagingData<TitleItemFull>> = withContext(dispatcher) {
        val genresList = genresRepository.getAvailableGenres()
        return@withContext Pager(
            config = getPagingConfig(),
            pagingSourceFactory = { localDataSource.getCachedDiscoverTV() },
            remoteMediator = titlesRemoteMediatorProvider.getDiscoverTVRemoteMediator(
                filter = filter,
                genres = genresList
            )
        ).flow.map { pagingData ->
            pagingData.map { it.toTitleItemFull() }
        }
    }

    override suspend fun getPopularMovies(): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            getTitleItemsFullResult(requestType = TitleItemsRequestType.PopularMovies)
        }

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun getPopularMoviesPaginated(
        filter: TitleListFilter
    ): Flow<PagingData<TitleItemFull>> = withContext(dispatcher) {
        val genresList = genresRepository.getAvailableGenres()
        return@withContext Pager(
            config = getPagingConfig(),
            pagingSourceFactory = { localDataSource.getCachedPopularMovies() },
            remoteMediator = titlesRemoteMediatorProvider.getPopularMoviesRemoteMediator(
                filter = filter,
                genres = genresList
            )
        ).flow.map { pagingData ->
            pagingData.map { it.toTitleItemFull() }
        }
    }

    override suspend fun getPopularTV(): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        getTitleItemsFullResult(requestType = TitleItemsRequestType.PopularTV)
    }

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun getPopularTvPaginated(
        filter: TitleListFilter
    ): Flow<PagingData<TitleItemFull>> = withContext(dispatcher) {
        val genresList = genresRepository.getAvailableGenres()
        return@withContext Pager(
            config = getPagingConfig(),
            pagingSourceFactory = { localDataSource.getCachedPopularTV() },
            remoteMediator = titlesRemoteMediatorProvider.getPopularTVRemoteMediator(
                filter = filter,
                genres = genresList
            )
        ).flow.map { pagingData ->
            pagingData.map { it.toTitleItemFull() }
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun searchAllPaginated(query: String): Flow<PagingData<TitleItemFull>> =
        withContext(dispatcher) {
            val genresList = genresRepository.getAvailableGenres()
            return@withContext Pager(
                config = getPagingConfig(),
                pagingSourceFactory = { localDataSource.getCachedSearchAll() },
                remoteMediator = titlesRemoteMediatorProvider.getSearchAllRemoteMediator(
                    query = query,
                    genres = genresList
                )
            ).flow.map { pagingData ->
                pagingData.map { it.toTitleItemFull() }
            }
        }

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun searchMoviesPaginated(query: String): Flow<PagingData<TitleItemFull>> =
        withContext(dispatcher) {
            val genresList = genresRepository.getAvailableGenres()
            return@withContext Pager(
                config = getPagingConfig(),
                pagingSourceFactory = { localDataSource.getCachedSearchMovies() },
                remoteMediator = titlesRemoteMediatorProvider.getSearchMoviesRemoteMediator(
                    query = query,
                    genres = genresList
                )
            ).flow.map { pagingData ->
                pagingData.map { it.toTitleItemFull() }
            }
        }

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun searchTVPaginated(query: String): Flow<PagingData<TitleItemFull>> =
        withContext(dispatcher) {
            val genresList = genresRepository.getAvailableGenres()
            return@withContext Pager(
                config = getPagingConfig(),
                pagingSourceFactory = { localDataSource.getCachedSearchTV() },
                remoteMediator = titlesRemoteMediatorProvider.getSearchTVRemoteMediator(
                    query = query,
                    genres = genresList
                )
            ).flow.map { pagingData ->
                pagingData.map { it.toTitleItemFull() }
            }
        }

    override suspend fun getTopRatedMovies(): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            getTitleItemsFullResult(requestType = TitleItemsRequestType.TopRatedMovies)
        }

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun getTopRatedMoviesPaginated(
        filter: TitleListFilter
    ): Flow<PagingData<TitleItemFull>> = withContext(dispatcher) {
        val genresList = genresRepository.getAvailableGenres()
        return@withContext Pager(
            config = getPagingConfig(),
            pagingSourceFactory = { localDataSource.getCachedTopRatedMovies() },
            remoteMediator = titlesRemoteMediatorProvider.getTopRatedMoviesRemoteMediator(
                filter = filter,
                genres = genresList
            )
        ).flow.map { pagingData ->
            pagingData.map { it.toTitleItemFull() }
        }
    }

    override suspend fun getTopRatedTV(): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        getTitleItemsFullResult(requestType = TitleItemsRequestType.TopRatedTV)
    }

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun getTopRatedTVPaginated(
        filter: TitleListFilter
    ): Flow<PagingData<TitleItemFull>> = withContext(dispatcher) {
        val genresList = genresRepository.getAvailableGenres()
        return@withContext Pager(
            config = getPagingConfig(),
            pagingSourceFactory = { localDataSource.getCachedTopRatedTV() },
            remoteMediator = titlesRemoteMediatorProvider.getTopRatedTVRemoteMediator(
                filter = filter,
                genres = genresList
            )
        ).flow.map { pagingData ->
            pagingData.map { it.toTitleItemFull() }
        }
    }

    override suspend fun getUpcomingMovies(): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            getTitleItemsFullResult(requestType = TitleItemsRequestType.UpcomingMovies)
        }

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun getUpcomingMoviesPaginated(
        filter: TitleListFilter
    ): Flow<PagingData<TitleItemFull>> = withContext(dispatcher) {
        val genresList = genresRepository.getAvailableGenres()
        return@withContext Pager(
            config = getPagingConfig(),
            pagingSourceFactory = { localDataSource.getCachedUpcomingMovies() },
            remoteMediator = titlesRemoteMediatorProvider.getUpcomingMoviesRemoteMediator(
                filter = filter,
                genres = genresList
            )
        ).flow.map { pagingData ->
            pagingData.map { it.toTitleItemFull() }
        }
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
            TitleItemsRequestType.PopularMovies ->
                remoteDataSource.getPopularMovies(allGenres = genresList)
            TitleItemsRequestType.PopularTV ->
                remoteDataSource.getPopularTV(allGenres = genresList)
            TitleItemsRequestType.TopRatedMovies ->
                remoteDataSource.getTopRatedMovies(allGenres = genresList)
            TitleItemsRequestType.TopRatedTV ->
                remoteDataSource.getTopRatedTV(allGenres = genresList)
            TitleItemsRequestType.UpcomingMovies ->
                remoteDataSource.getUpcomingMovies(allGenres = genresList)
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

    /**
     * A convenience method for returning a [PagingConfig], in most cases the default version should
     * be used.
     */
    private fun getPagingConfig(
        pageSize: Int = Constants.PAGE_SIZE,
        prefetchDistance: Int = 2 * Constants.PAGE_SIZE,
        enablePlaceholders: Boolean = false,
        initialLoadSize: Int = 4 * Constants.PAGE_SIZE,
        maxSize: Int = 200,
    ): PagingConfig = PagingConfig(
        pageSize = pageSize,
        prefetchDistance = prefetchDistance,
        enablePlaceholders = enablePlaceholders,
        initialLoadSize = initialLoadSize,
        maxSize = maxSize
    )

    override fun getRecentSearches(): Flow<List<RecentSearch>> = localDataSource.getRecentSearches()

    override suspend fun saveNewRecentSearch(newSearch: String) = withContext(dispatcher) {
        localDataSource.saveNewRecentSearch(newSearch = newSearch)
    }

    private sealed class TitleItemsRequestType {
        object TrendingMoviesAndTV : TitleItemsRequestType()
        object PopularMovies : TitleItemsRequestType()
        object PopularTV : TitleItemsRequestType()
        object UpcomingMovies : TitleItemsRequestType()
        object TopRatedMovies : TitleItemsRequestType()
        object TopRatedTV : TitleItemsRequestType()
    }
}