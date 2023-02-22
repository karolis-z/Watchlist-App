@file:OptIn(ExperimentalPagingApi::class)

package com.myapplications.mywatchlist.data.mediators

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.myapplications.mywatchlist.core.util.Constants.CACHING_TIMEOUT
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.data.entities.cached.*
import com.myapplications.mywatchlist.data.local.WatchlistDatabase
import com.myapplications.mywatchlist.data.remote.TitlesRemoteDataSource
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleListFilter
import com.myapplications.mywatchlist.domain.result.ResultOf
import java.time.Instant
import javax.inject.Inject

private const val TAG = "MEDIATOR"

class TitlesRemoteMediatorProviderImpl @Inject constructor(
    private val titlesRemoteDataSource: TitlesRemoteDataSource,
    private val database: WatchlistDatabase
) : TitlesRemoteMediatorProvider {

    override fun getDiscoverMovieRemoteMediator(
        filter: TitleListFilter,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCacheDiscoverMovieFull> {
        return DiscoverMoviesRemoteMediator(
            requestType = TitleItemsRequestType.DiscoverMovies(filter),
            genres = genres
        )
    }

    override fun getDiscoverTVRemoteMediator(
        filter: TitleListFilter,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCacheDiscoverTVFull> {
        return DiscoverTVRemoteMediator(
            requestType = TitleItemsRequestType.DiscoverTV(filter),
            genres = genres
        )
    }

    override fun getPopularMoviesRemoteMediator(
        filter: TitleListFilter,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCachePopularMovieFull> {
        return PopularMoviesRemoteMediator(
            requestType = TitleItemsRequestType.PopularMovies(filter),
            genres = genres
        )
    }

    override fun getPopularTVRemoteMediator(
        filter: TitleListFilter,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCachePopularTVFull> {
        return PopularTVRemoteMediator(
            requestType = TitleItemsRequestType.PopularTV(filter),
            genres = genres
        )
    }

    override fun getSearchAllRemoteMediator(
        query: String,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCacheSearchAllFull> {
        return SearchAllRemoteMediator(
            requestType = TitleItemsRequestType.SearchAll(query),
            genres = genres
        )
    }

    override fun getSearchMoviesRemoteMediator(
        query: String,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCacheSearchMovieFull> {
        return SearchMoviesRemoteMediator(
            requestType = TitleItemsRequestType.SearchMovies(query),
            genres = genres
        )
    }

    override fun getSearchTVRemoteMediator(
        query: String,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCacheSearchTVFull> {
        return SearchTVRemoteMediator(
            requestType = TitleItemsRequestType.SearchTV(query),
            genres = genres
        )
    }

    override fun getTopRatedMoviesRemoteMediator(
        filter: TitleListFilter,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCacheTopRatedMovieFull> {
        return TopRatedMoviesRemoteMediator(
            requestType = TitleItemsRequestType.TopRatedMovies(filter),
            genres = genres
        )
    }

    override fun getTopRatedTVRemoteMediator(
        filter: TitleListFilter,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCacheTopRatedTVFull> {
        return TopRatedTVRemoteMediator(
            requestType = TitleItemsRequestType.TopRatedTV(filter),
            genres = genres
        )
    }

    override fun getUpcomingMoviesRemoteMediator(
        filter: TitleListFilter,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCacheUpcomingMovieFull> {
        return UpcomingMoviesRemoteMediator(
            requestType = TitleItemsRequestType.UpcomingMovies(filter),
            genres = genres
        )
    }

    inner class DiscoverMoviesRemoteMediator(
        private val requestType: TitleItemsRequestType,
        private val genres: List<Genre>
    ) : RemoteMediator<Int, TitleItemCacheDiscoverMovieFull>() {
        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, TitleItemCacheDiscoverMovieFull>
        ): MediatorResult {
            return loadData(loadType, state, requestType, genres)
        }

        override suspend fun initialize(): InitializeAction = initializeMediator(requestType)
    }

    inner class DiscoverTVRemoteMediator(
        private val requestType: TitleItemsRequestType,
        private val genres: List<Genre>
    ) : RemoteMediator<Int, TitleItemCacheDiscoverTVFull>() {
        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, TitleItemCacheDiscoverTVFull>
        ): MediatorResult {
            return loadData(loadType, state, requestType, genres)
        }

        override suspend fun initialize(): InitializeAction = initializeMediator(requestType)
    }

    inner class PopularMoviesRemoteMediator(
        private val requestType: TitleItemsRequestType,
        private val genres: List<Genre>
    ) : RemoteMediator<Int, TitleItemCachePopularMovieFull>() {
        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, TitleItemCachePopularMovieFull>
        ): MediatorResult {
            return loadData(loadType, state, requestType, genres)
        }

        override suspend fun initialize(): InitializeAction = initializeMediator(requestType)
    }

    inner class PopularTVRemoteMediator(
        private val requestType: TitleItemsRequestType,
        private val genres: List<Genre>
    ) : RemoteMediator<Int, TitleItemCachePopularTVFull>() {
        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, TitleItemCachePopularTVFull>
        ): MediatorResult {
            return loadData(loadType, state, requestType, genres)
        }

        override suspend fun initialize(): InitializeAction = initializeMediator(requestType)
    }

    inner class SearchAllRemoteMediator(
        private val requestType: TitleItemsRequestType,
        private val genres: List<Genre>
    ) : RemoteMediator<Int, TitleItemCacheSearchAllFull>() {
        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, TitleItemCacheSearchAllFull>
        ): MediatorResult {
            return loadData(loadType, state, requestType, genres)
        }

        override suspend fun initialize(): InitializeAction = initializeMediator(requestType)
    }

    inner class SearchMoviesRemoteMediator(
        private val requestType: TitleItemsRequestType,
        private val genres: List<Genre>
    ) : RemoteMediator<Int, TitleItemCacheSearchMovieFull>() {
        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, TitleItemCacheSearchMovieFull>
        ): MediatorResult {
            return loadData(loadType, state, requestType, genres)
        }

        override suspend fun initialize(): InitializeAction = initializeMediator(requestType)
    }

    inner class SearchTVRemoteMediator(
        private val requestType: TitleItemsRequestType,
        private val genres: List<Genre>
    ) : RemoteMediator<Int, TitleItemCacheSearchTVFull>() {
        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, TitleItemCacheSearchTVFull>
        ): MediatorResult {
            return loadData(loadType, state, requestType, genres)
        }

        override suspend fun initialize(): InitializeAction = initializeMediator(requestType)
    }

    inner class TopRatedMoviesRemoteMediator(
        private val requestType: TitleItemsRequestType,
        private val genres: List<Genre>
    ) : RemoteMediator<Int, TitleItemCacheTopRatedMovieFull>() {
        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, TitleItemCacheTopRatedMovieFull>
        ): MediatorResult {
            return loadData(loadType, state, requestType, genres)
        }

        override suspend fun initialize(): InitializeAction = initializeMediator(requestType)
    }

    inner class TopRatedTVRemoteMediator(
        private val requestType: TitleItemsRequestType,
        private val genres: List<Genre>
    ) : RemoteMediator<Int, TitleItemCacheTopRatedTVFull>() {
        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, TitleItemCacheTopRatedTVFull>
        ): MediatorResult {
            return loadData(loadType, state, requestType, genres)
        }

        override suspend fun initialize(): InitializeAction = initializeMediator(requestType)
    }

    inner class UpcomingMoviesRemoteMediator(
        private val requestType: TitleItemsRequestType,
        private val genres: List<Genre>
    ) : RemoteMediator<Int, TitleItemCacheUpcomingMovieFull>() {
        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, TitleItemCacheUpcomingMovieFull>
        ): MediatorResult {
            return loadData(loadType, state, requestType, genres)
        }

        override suspend fun initialize(): InitializeAction = initializeMediator(requestType)
    }

    private suspend fun <T : TitleItemCacheFull> loadData(
        loadType: LoadType,
        state: PagingState<Int, T>,
        requestType: TitleItemsRequestType,
        genres: List<Genre>
    ): RemoteMediator.MediatorResult {
        val page: Int = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(
                    state = state,
                    requestType = requestType
                )
                remoteKeys?.nextKey?.minus(1) ?: 1
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state = state, requestType = requestType)
                val prevKey = remoteKeys?.prevKey
                prevKey
                    ?: return RemoteMediator.MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state = state, requestType = requestType)
                val nextKey = remoteKeys?.nextKey
                nextKey
                    ?: return RemoteMediator.MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
        }

        val apiResult = when (requestType) {
            is TitleItemsRequestType.DiscoverMovies ->
                titlesRemoteDataSource.getDiscoverMovies(
                    page = page,
                    filter = requestType.filter,
                    allGenres = genres
                )
            is TitleItemsRequestType.DiscoverTV ->
                titlesRemoteDataSource.getDiscoverTV(
                    page = page,
                    filter = requestType.filter,
                    allGenres = genres
                )
            is TitleItemsRequestType.PopularMovies ->
                titlesRemoteDataSource.getPopularMovies(
                    page = page,
                    filter = requestType.filter,
                    allGenres = genres
                )
            is TitleItemsRequestType.PopularTV ->
                titlesRemoteDataSource.getPopularTV(
                    page = page,
                    filter = requestType.filter,
                    allGenres = genres
                )
            is TitleItemsRequestType.SearchAll ->
                titlesRemoteDataSource.searchAllTitles(
                    page = page,
                    query = requestType.query,
                    allGenres = genres
                )
            is TitleItemsRequestType.SearchMovies ->
                titlesRemoteDataSource.searchMovies(
                    page = page,
                    query = requestType.query,
                    allGenres = genres
                )
            is TitleItemsRequestType.SearchTV ->
                titlesRemoteDataSource.searchTV(
                    page = page,
                    query = requestType.query,
                    allGenres = genres
                )
            is TitleItemsRequestType.TopRatedMovies ->
                titlesRemoteDataSource.getTopRatedMovies(
                    page = page,
                    filter = requestType.filter,
                    allGenres = genres
                )
            is TitleItemsRequestType.TopRatedTV ->
                titlesRemoteDataSource.getTopRatedTV(
                    page = page,
                    filter = requestType.filter,
                    allGenres = genres
                )
            is TitleItemsRequestType.UpcomingMovies ->
                titlesRemoteDataSource.getUpcomingMovies(
                    page = page,
                    filter = requestType.filter,
                    allGenres = genres
                )
        }

        val endOfPaginationReached = when (apiResult) {
            is ResultOf.Failure -> {
                if (apiResult.throwable !is ApiGetTitleItemsExceptions) {
                    return RemoteMediator.MediatorResult.Error(
                        Exception("Unknown Error getting page of data from the api")
                    )
                } else {
                    when (apiResult.throwable) {
                        is ApiGetTitleItemsExceptions.FailedApiRequestException,
                        is ApiGetTitleItemsExceptions.NoConnectionException -> {
                            return RemoteMediator.MediatorResult.Error(apiResult.throwable)
                        }
                        is ApiGetTitleItemsExceptions.NothingFoundException -> {
                            true
                        }
                    }
                }
            }
            is ResultOf.Success -> false
        }
        if (!endOfPaginationReached) {

            val titleList = (apiResult as ResultOf.Success).data

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    clearDataOnRefresh(requestType = requestType)
                }

                val prevKey = if (page > 1) page - 1 else null
                val nextKey = if (endOfPaginationReached) null else page + 1

                when (requestType) {
                    is TitleItemsRequestType.DiscoverMovies -> {
                        database.discoverMoviesCacheDao().insertCachedTrendingItems(
                            titlesList = titleList,
                            page = page,
                            prevKey = prevKey,
                            nextKey = nextKey,
                            createdOn = Instant.now().toEpochMilli()
                        )
                    }
                    is TitleItemsRequestType.DiscoverTV -> {
                        database.discoverTvCacheDao().insertCachedTrendingItems(
                            titlesList = titleList,
                            page = page,
                            prevKey = prevKey,
                            nextKey = nextKey,
                            createdOn = Instant.now().toEpochMilli()
                        )
                    }
                    is TitleItemsRequestType.PopularMovies -> {
                        database.popularMoviesCacheDao().insertCachedTrendingItems(
                            titlesList = titleList,
                            page = page,
                            prevKey = prevKey,
                            nextKey = nextKey,
                            createdOn = Instant.now().toEpochMilli()
                        )
                    }
                    is TitleItemsRequestType.PopularTV -> {
                        database.popularTvCacheDao().insertCachedTrendingItems(
                            titlesList = titleList,
                            page = page,
                            prevKey = prevKey,
                            nextKey = nextKey,
                            createdOn = Instant.now().toEpochMilli()
                        )
                    }
                    is TitleItemsRequestType.SearchAll -> {
                        database.searchAllCacheDao().insertCachedTrendingItems(
                            titlesList = titleList,
                            page = page,
                            prevKey = prevKey,
                            nextKey = nextKey,
                            createdOn = Instant.now().toEpochMilli()
                        )
                    }
                    is TitleItemsRequestType.SearchMovies -> {
                        database.searchMoviesCacheDao().insertCachedTrendingItems(
                            titlesList = titleList,
                            page = page,
                            prevKey = prevKey,
                            nextKey = nextKey,
                            createdOn = Instant.now().toEpochMilli()
                        )
                    }
                    is TitleItemsRequestType.SearchTV -> {
                        database.searchTvCacheDao().insertCachedTrendingItems(
                            titlesList = titleList,
                            page = page,
                            prevKey = prevKey,
                            nextKey = nextKey,
                            createdOn = Instant.now().toEpochMilli()
                        )
                    }
                    is TitleItemsRequestType.TopRatedMovies -> {
                        database.topRatedMoviesCacheDao().insertCachedTrendingItems(
                            titlesList = titleList,
                            page = page,
                            prevKey = prevKey,
                            nextKey = nextKey,
                            createdOn = Instant.now().toEpochMilli()
                        )
                    }
                    is TitleItemsRequestType.TopRatedTV -> {
                        database.topRatedTvCacheDao().insertCachedTrendingItems(
                            titlesList = titleList,
                            page = page,
                            prevKey = prevKey,
                            nextKey = nextKey,
                            createdOn = Instant.now().toEpochMilli()
                        )
                    }
                    is TitleItemsRequestType.UpcomingMovies -> {
                        database.upcomingMoviesDao().insertCachedTrendingItems(
                            titlesList = titleList,
                            page = page,
                            prevKey = prevKey,
                            nextKey = nextKey,
                            createdOn = Instant.now().toEpochMilli()
                        )
                    }
                }
            }
        }
        return RemoteMediator.MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
    }

    private suspend fun initializeMediator(
        requestType: TitleItemsRequestType
    ): RemoteMediator.InitializeAction {
        val creationTime = when (requestType) {
            is TitleItemsRequestType.DiscoverMovies ->
                database.discoverMoviesCacheDao().getCreationTime() ?: 0
            is TitleItemsRequestType.DiscoverTV ->
                database.discoverTvCacheDao().getCreationTime() ?: 0
            is TitleItemsRequestType.PopularMovies ->
                database.popularMoviesCacheDao().getCreationTime() ?: 0
            is TitleItemsRequestType.PopularTV ->
                database.popularTvCacheDao().getCreationTime() ?: 0
            is TitleItemsRequestType.SearchAll ->
                database.searchAllCacheDao().getCreationTime() ?: 0
            is TitleItemsRequestType.SearchMovies ->
                database.searchMoviesCacheDao().getCreationTime() ?: 0
            is TitleItemsRequestType.SearchTV ->
                database.searchTvCacheDao().getCreationTime() ?: 0
            is TitleItemsRequestType.TopRatedMovies ->
                database.topRatedMoviesCacheDao().getCreationTime() ?: 0
            is TitleItemsRequestType.TopRatedTV ->
                database.topRatedTvCacheDao().getCreationTime() ?: 0
            is TitleItemsRequestType.UpcomingMovies ->
                database.upcomingMoviesDao().getCreationTime() ?: 0
        }
        val timeDifference = Instant.now().toEpochMilli() - creationTime
        return if (timeDifference < CACHING_TIMEOUT) {
            RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    private suspend fun clearDataOnRefresh(requestType: TitleItemsRequestType) {
        when (requestType) {
            is TitleItemsRequestType.DiscoverMovies -> {
                database.discoverMoviesCacheDao().clearRemoteKeys()
                database.discoverMoviesCacheDao().clearAllCachedTitles()
            }
            is TitleItemsRequestType.DiscoverTV -> {
                database.discoverTvCacheDao().clearRemoteKeys()
                database.discoverTvCacheDao().clearAllCachedTitles()
            }
            is TitleItemsRequestType.PopularMovies -> {
                database.popularMoviesCacheDao().clearRemoteKeys()
                database.popularMoviesCacheDao().clearAllCachedTitles()
            }
            is TitleItemsRequestType.PopularTV -> {
                database.popularTvCacheDao().clearRemoteKeys()
                database.popularTvCacheDao().clearAllCachedTitles()
            }
            is TitleItemsRequestType.SearchAll -> {
                database.searchAllCacheDao().clearRemoteKeys()
                database.searchAllCacheDao().clearAllCachedTitles()
            }
            is TitleItemsRequestType.SearchMovies -> {
                database.searchMoviesCacheDao().clearRemoteKeys()
                database.searchMoviesCacheDao().clearAllCachedTitles()
            }
            is TitleItemsRequestType.SearchTV -> {
                database.searchTvCacheDao().clearRemoteKeys()
                database.searchTvCacheDao().clearAllCachedTitles()
            }
            is TitleItemsRequestType.TopRatedMovies -> {
                database.topRatedMoviesCacheDao().clearRemoteKeys()
                database.topRatedMoviesCacheDao().clearAllCachedTitles()
            }
            is TitleItemsRequestType.TopRatedTV -> {
                database.topRatedTvCacheDao().clearRemoteKeys()
                database.topRatedTvCacheDao().clearAllCachedTitles()
            }
            is TitleItemsRequestType.UpcomingMovies -> {
                database.upcomingMoviesDao().clearRemoteKeys()
                database.upcomingMoviesDao().clearAllCachedTitles()
            }
        }
    }

    private suspend fun <T : TitleItemCacheFull> getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, T>,
        requestType: TitleItemsRequestType
    ): RemoteKey? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.titleItem?.id?.let { id ->
                getRemoteKeyFromDaoById(id, requestType)
            }
        }
    }

    private suspend fun <T : TitleItemCacheFull> getRemoteKeyForFirstItem(
        state: PagingState<Int, T>,
        requestType: TitleItemsRequestType
    ): RemoteKey? {
        return state.pages.firstOrNull {
            it.data.isNotEmpty()
        }?.data?.firstOrNull()?.let { titleItem ->
            getRemoteKeyFromDaoById(titleItem.titleItem.id, requestType)
        }
    }

    private suspend fun <T : TitleItemCacheFull> getRemoteKeyForLastItem(
        state: PagingState<Int, T>,
        requestType: TitleItemsRequestType
    ): RemoteKey? {
        return state.pages.lastOrNull {
            it.data.isNotEmpty()
        }?.data?.lastOrNull()?.let { titleItem ->
            getRemoteKeyFromDaoById(titleItem.titleItem.id, requestType)
        }
    }

    private suspend fun getRemoteKeyFromDaoById(
        id: Long,
        requestType: TitleItemsRequestType
    ): RemoteKey? {
        return when (requestType) {
            is TitleItemsRequestType.DiscoverMovies ->
                database.discoverMoviesCacheDao().getRemoteKeyById(id)
            is TitleItemsRequestType.DiscoverTV ->
                database.discoverTvCacheDao().getRemoteKeyById(id)
            is TitleItemsRequestType.PopularMovies ->
                database.popularMoviesCacheDao().getRemoteKeyById(id)
            is TitleItemsRequestType.PopularTV ->
                database.popularTvCacheDao().getRemoteKeyById(id)
            is TitleItemsRequestType.SearchAll ->
                database.searchAllCacheDao().getRemoteKeyById(id)
            is TitleItemsRequestType.SearchMovies ->
                database.searchMoviesCacheDao().getRemoteKeyById(id)
            is TitleItemsRequestType.SearchTV ->
                database.searchTvCacheDao().getRemoteKeyById(id)
            is TitleItemsRequestType.TopRatedMovies ->
                database.topRatedMoviesCacheDao().getRemoteKeyById(id)
            is TitleItemsRequestType.TopRatedTV ->
                database.topRatedTvCacheDao().getRemoteKeyById(id)
            is TitleItemsRequestType.UpcomingMovies ->
                database.upcomingMoviesDao().getRemoteKeyById(id)
        }
    }

    sealed class TitleItemsRequestType {
        data class PopularMovies(val filter: TitleListFilter) : TitleItemsRequestType()
        data class PopularTV(val filter: TitleListFilter) : TitleItemsRequestType()
        data class UpcomingMovies(val filter: TitleListFilter) : TitleItemsRequestType()
        data class TopRatedMovies(val filter: TitleListFilter) : TitleItemsRequestType()
        data class TopRatedTV(val filter: TitleListFilter) : TitleItemsRequestType()
        data class SearchAll(val query: String) : TitleItemsRequestType()
        data class SearchMovies(val query: String) : TitleItemsRequestType()
        data class SearchTV(val query: String) : TitleItemsRequestType()
        data class DiscoverMovies(val filter: TitleListFilter) : TitleItemsRequestType()
        data class DiscoverTV(val filter: TitleListFilter) : TitleItemsRequestType()
    }

}

//@OptIn(ExperimentalPagingApi::class)
//class TitlesTrendingRemoteMediator (
//    private val titlesRemoteDataSource: TitlesRemoteDataSource,
//    private val database: WatchlistDatabase,
//    private val requestType: TitleItemsRequestType,
//    private val genres: List<Genre>
//) : RemoteMediator<Int, TitleItemCacheFull>() {
//
//    override suspend fun load(
//        loadType: LoadType,
//        state: PagingState<Int, TitleItemCacheFull>
//    ): MediatorResult {
//        val page: Int = when (loadType) {
//            LoadType.REFRESH -> {
//                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
//                remoteKeys?.nextKey?.minus(1) ?: 1
//            }
//            LoadType.PREPEND -> {
//                val remoteKeys = getRemoteKeyForFirstItem(state)
//                val prevKey = remoteKeys?.prevKey
//                prevKey
//                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
//            }
//            LoadType.APPEND -> {
//                val remoteKeys = getRemoteKeyForLastItem(state)
//                val nextKey = remoteKeys?.nextKey
//                nextKey
//                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
//            }
//        }
//
//        val apiResult = when (requestType) {
//            is TitleItemsRequestType.DiscoverMovies ->
//                titlesRemoteDataSource.getDiscoverMovies(page = page, filter = requestType.filter, allGenres = genres)
//            is TitleItemsRequestType.DiscoverTV ->
//                titlesRemoteDataSource.getDiscoverTV(page = page, filter = requestType.filter, allGenres = genres)
//            is TitleItemsRequestType.PopularMovies ->
//                titlesRemoteDataSource.getPopularMovies(page = page, filter = requestType.filter, allGenres = genres)
//            is TitleItemsRequestType.PopularTV ->
//                titlesRemoteDataSource.getPopularTV(page = page, filter = requestType.filter, allGenres = genres)
//            is TitleItemsRequestType.SearchAll ->
//                titlesRemoteDataSource.searchAllTitles(page = page, query = requestType.query, allGenres = genres)
//            is TitleItemsRequestType.SearchMovies ->
//                titlesRemoteDataSource.searchMovies(page = page, query = requestType.query, allGenres = genres)
//            is TitleItemsRequestType.SearchTV ->
//                titlesRemoteDataSource.searchTV(page = page, query = requestType.query, allGenres = genres)
//            is TitleItemsRequestType.TopRatedMovies ->
//                titlesRemoteDataSource.getTopRatedMovies(page = page, filter = requestType.filter, allGenres = genres)
//            is TitleItemsRequestType.TopRatedTV ->
//                titlesRemoteDataSource.getTopRatedTV(page = page, filter = requestType.filter, allGenres = genres)
//            is TitleItemsRequestType.UpcomingMovies ->
//                titlesRemoteDataSource.getUpcomingMovies(page = page, filter = requestType.filter, allGenres = genres)
//        }
//
//        val endOfPaginationReached = when (apiResult) {
//            is ResultOf.Failure -> {
//                if (apiResult.throwable !is ApiGetTitleItemsExceptions) {
//                    return MediatorResult.Error(
//                        Exception("Unknown Error getting page of data from the api")
//                    )
//                } else {
//                    when (apiResult.throwable) {
//                        is ApiGetTitleItemsExceptions.FailedApiRequestException,
//                        is ApiGetTitleItemsExceptions.NoConnectionException -> {
//                            return MediatorResult.Error(apiResult.throwable)
//                        }
//                        is ApiGetTitleItemsExceptions.NothingFoundException -> {
//                            true
//                        }
//                    }
//                }
//            }
//            is ResultOf.Success -> false
//        }
//        if (!endOfPaginationReached) {
//
//            val titleList = (apiResult as ResultOf.Success).data
//
//            database.withTransaction {
//                if (loadType == LoadType.REFRESH) {
//
//                    val prevKey = if (page > 1) page - 1 else null
//                    val nextKey = if (endOfPaginationReached) null else page + 1
//
//                    when (requestType) {
//                        is TitleItemsRequestType.DiscoverMovies -> {
//                            database.discoverMoviesCacheDao().clearRemoteKeys()
//                            database.discoverMoviesCacheDao().clearAllCachedTitles()
//                            database.discoverMoviesCacheDao().insertCachedTrendingItems(
//                                titlesList = titleList,
//                                page = page,
//                                prevKey = prevKey,
//                                nextKey = nextKey,
//                                createdOn = Instant.now().toEpochMilli()
//                            )
//                        }
//                        is TitleItemsRequestType.DiscoverTV -> {
//                            database.discoverTvCacheDao().clearRemoteKeys()
//                            database.discoverTvCacheDao().clearAllCachedTitles()
//                            database.discoverTvCacheDao().insertCachedTrendingItems(
//                                titlesList = titleList,
//                                page = page,
//                                prevKey = prevKey,
//                                nextKey = nextKey,
//                                createdOn = Instant.now().toEpochMilli()
//                            )
//                        }
//                        is TitleItemsRequestType.PopularMovies -> {
//                            database.popularMoviesCacheDao().clearRemoteKeys()
//                            database.popularMoviesCacheDao().clearAllCachedTitles()
//                            database.popularMoviesCacheDao().insertCachedTrendingItems(
//                                titlesList = titleList,
//                                page = page,
//                                prevKey = prevKey,
//                                nextKey = nextKey,
//                                createdOn = Instant.now().toEpochMilli()
//                            )
//                        }
//                        is TitleItemsRequestType.PopularTV -> {
//                            database.popularTvCacheDao().clearRemoteKeys()
//                            database.popularTvCacheDao().clearAllCachedTitles()
//                            database.popularTvCacheDao().insertCachedTrendingItems(
//                                titlesList = titleList,
//                                page = page,
//                                prevKey = prevKey,
//                                nextKey = nextKey,
//                                createdOn = Instant.now().toEpochMilli()
//                            )
//                        }
//                        is TitleItemsRequestType.SearchAll -> {
//                            database.searchAllCacheDao().clearRemoteKeys()
//                            database.searchAllCacheDao().clearAllCachedTitles()
//                            database.searchAllCacheDao().insertCachedTrendingItems(
//                                titlesList = titleList,
//                                page = page,
//                                prevKey = prevKey,
//                                nextKey = nextKey,
//                                createdOn = Instant.now().toEpochMilli()
//                            )
//                        }
//                        is TitleItemsRequestType.SearchMovies -> {
//                            database.searchMoviesCacheDao().clearRemoteKeys()
//                            database.searchMoviesCacheDao().clearAllCachedTitles()
//                            database.searchMoviesCacheDao().insertCachedTrendingItems(
//                                titlesList = titleList,
//                                page = page,
//                                prevKey = prevKey,
//                                nextKey = nextKey,
//                                createdOn = Instant.now().toEpochMilli()
//                            )
//                        }
//                        is TitleItemsRequestType.SearchTV -> {
//                            database.searchTvCacheDao().clearRemoteKeys()
//                            database.searchTvCacheDao().clearAllCachedTitles()
//                            database.searchTvCacheDao().insertCachedTrendingItems(
//                                titlesList = titleList,
//                                page = page,
//                                prevKey = prevKey,
//                                nextKey = nextKey,
//                                createdOn = Instant.now().toEpochMilli()
//                            )
//                        }
//                        is TitleItemsRequestType.TopRatedMovies -> {
//                            database.topRatedMoviesCacheDao().clearRemoteKeys()
//                            database.topRatedMoviesCacheDao().clearAllCachedTitles()
//                            database.topRatedMoviesCacheDao().insertCachedTrendingItems(
//                                titlesList = titleList,
//                                page = page,
//                                prevKey = prevKey,
//                                nextKey = nextKey,
//                                createdOn = Instant.now().toEpochMilli()
//                            )
//                        }
//                        is TitleItemsRequestType.TopRatedTV -> {
//                            database.topRatedTvCacheDao().clearRemoteKeys()
//                            database.topRatedTvCacheDao().clearAllCachedTitles()
//                            database.topRatedTvCacheDao().insertCachedTrendingItems(
//                                titlesList = titleList,
//                                page = page,
//                                prevKey = prevKey,
//                                nextKey = nextKey,
//                                createdOn = Instant.now().toEpochMilli()
//                            )
//                        }
//                        is TitleItemsRequestType.UpcomingMovies -> {
//                            database.upcomingMoviesDao().clearRemoteKeys()
//                            database.upcomingMoviesDao().clearAllCachedTitles()
//                            database.upcomingMoviesDao().insertCachedTrendingItems(
//                                titlesList = titleList,
//                                page = page,
//                                prevKey = prevKey,
//                                nextKey = nextKey,
//                                createdOn = Instant.now().toEpochMilli()
//                            )
//                        }
//                    }
//                }
//            }
//        }
//        return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
//    }
//
//    override suspend fun initialize(): InitializeAction {
//        val creationTime = when (requestType) {
//            is TitleItemsRequestType.DiscoverMovies ->
//                database.discoverMoviesCacheDao().getCreationTime() ?: 0
//            is TitleItemsRequestType.DiscoverTV ->
//                database.discoverTvCacheDao().getCreationTime() ?: 0
//            is TitleItemsRequestType.PopularMovies ->
//                database.popularMoviesCacheDao().getCreationTime() ?: 0
//            is TitleItemsRequestType.PopularTV ->
//                database.popularTvCacheDao().getCreationTime() ?: 0
//            is TitleItemsRequestType.SearchAll ->
//                database.searchAllCacheDao().getCreationTime() ?: 0
//            is TitleItemsRequestType.SearchMovies ->
//                database.searchMoviesCacheDao().getCreationTime() ?: 0
//            is TitleItemsRequestType.SearchTV ->
//                database.searchTvCacheDao().getCreationTime() ?: 0
//            is TitleItemsRequestType.TopRatedMovies ->
//                database.topRatedMoviesCacheDao().getCreationTime() ?: 0
//            is TitleItemsRequestType.TopRatedTV ->
//                database.topRatedTvCacheDao().getCreationTime() ?: 0
//            is TitleItemsRequestType.UpcomingMovies ->
//                database.upcomingMoviesDao().getCreationTime() ?: 0
//        }
//        val timeDifference = Instant.now().toEpochMilli() - creationTime
//        return if (timeDifference < CACHING_TIMEOUT) {
//            InitializeAction.SKIP_INITIAL_REFRESH
//        } else {
//            InitializeAction.LAUNCH_INITIAL_REFRESH
//        }
//    }
//
//    private suspend fun getRemoteKeyClosestToCurrentPosition(
//        state: PagingState<Int, TitleItemCacheFull>
//    ): RemoteKey? {
//        return state.anchorPosition?.let { position ->
//            state.closestItemToPosition(position)?.titleItem?.id?.let { id ->
//                getRemoteKeyFromDaoById(id)
//            }
//        }
//    }
//
//    private suspend fun getRemoteKeyForFirstItem(
//        state: PagingState<Int, TitleItemCacheFull>
//    ): RemoteKey? {
//        return state.pages.firstOrNull {
//            it.data.isNotEmpty()
//        }?.data?.firstOrNull()?.let { titleItem ->
//            getRemoteKeyFromDaoById(titleItem.titleItem.id)
//        }
//    }
//
//    private suspend fun getRemoteKeyForLastItem(
//        state: PagingState<Int, TitleItemCacheFull>
//    ): RemoteKey? {
//        return state.pages.lastOrNull {
//            it.data.isNotEmpty()
//        }?.data?.lastOrNull()?.let { titleItem ->
//            getRemoteKeyFromDaoById(titleItem.titleItem.id)
//        }
//    }
//
//    private suspend fun getRemoteKeyFromDaoById(id: Long): RemoteKey? {
//        return when (requestType) {
//            is TitleItemsRequestType.DiscoverMovies ->
//                database.discoverMoviesCacheDao().getRemoteKeyById(id)
//            is TitleItemsRequestType.DiscoverTV ->
//                database.discoverTvCacheDao().getRemoteKeyById(id)
//            is TitleItemsRequestType.PopularMovies ->
//                database.popularMoviesCacheDao().getRemoteKeyById(id)
//            is TitleItemsRequestType.PopularTV ->
//                database.popularTvCacheDao().getRemoteKeyById(id)
//            is TitleItemsRequestType.SearchAll ->
//                database.searchAllCacheDao().getRemoteKeyById(id)
//            is TitleItemsRequestType.SearchMovies ->
//                database.searchMoviesCacheDao().getRemoteKeyById(id)
//            is TitleItemsRequestType.SearchTV ->
//                database.searchTvCacheDao().getRemoteKeyById(id)
//            is TitleItemsRequestType.TopRatedMovies ->
//                database.topRatedMoviesCacheDao().getRemoteKeyById(id)
//            is TitleItemsRequestType.TopRatedTV ->
//                database.topRatedTvCacheDao().getRemoteKeyById(id)
//            is TitleItemsRequestType.UpcomingMovies ->
//                database.upcomingMoviesDao().getRemoteKeyById(id)
//        }
//    }
//}