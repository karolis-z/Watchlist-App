package com.myapplications.mywatchlist.data.local.titles

import androidx.paging.PagingSource
import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.entities.RecentSearchEntity
import com.myapplications.mywatchlist.data.entities.cached.*
import com.myapplications.mywatchlist.data.local.titles.cache.*
import com.myapplications.mywatchlist.data.mappers.toRecentSearchList
import com.myapplications.mywatchlist.data.mappers.toTitleItemsFull
import com.myapplications.mywatchlist.domain.entities.RecentSearch
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

interface TitlesLocalDataSource {
    /**
     * Inserts the given [TitleItemFull] in the local database if it's not saved there already. And if
     * it is - it will update it in case the information has changed.
     */
    suspend fun bookmarkTitleItem(titleItemFull: TitleItemFull)

    /**
     * Deletes the given [TitleItemFull] from the local database.
     */
    suspend fun unBookmarkTitleItem(titleItemFull: TitleItemFull)

    /**
     * Returns titles stored in local database.
     * @return list of [TitleItemFull] or null if no [TitleItemFull]s are stored.
     */
    suspend fun getAllBookmarkedTitles(): List<TitleItemFull>?

    /**
     * @return [Boolean] indicating whether a [TitleItemFull] already is saved in local database as
     * watchlisted.
     */
    suspend fun checkIfTitleItemWatchlisted(titleItemFull: TitleItemFull): Boolean

    /**
     * @return a [Flow] of list of [TitleItemFull]s that are watchlisted.
     */
    fun allWatchlistedTitlesFlow(): Flow<List<TitleItemFull>>

    /**
     * @return a [PagingSource] for cached Discovered Movies.
     */
    fun getCachedDiscoverMovies(): PagingSource<Int, TitleItemCacheDiscoverMovieFull>

    /**
     * @return a [PagingSource] for cached Discovered TV Shows.
     */
    fun getCachedDiscoverTV(): PagingSource<Int, TitleItemCacheDiscoverTVFull>

    /**
     * @return a [PagingSource] for cached Popular Movies.
     */
    fun getCachedPopularMovies(): PagingSource<Int, TitleItemCachePopularMovieFull>

    /**
     * @return a [PagingSource] for cached Popular TV Shows.
     */
    fun getCachedPopularTV(): PagingSource<Int, TitleItemCachePopularTVFull>

    /**
     * @return a [PagingSource] for cached searched all titles.
     */
    fun getCachedSearchAll(): PagingSource<Int, TitleItemCacheSearchAllFull>

    /**
     * @return a [PagingSource] for cached searched Movies.
     */
    fun getCachedSearchMovies(): PagingSource<Int, TitleItemCacheSearchMovieFull>

    /**
     * @return a [PagingSource] for cached searched TV shows.
     */
    fun getCachedSearchTV(): PagingSource<Int, TitleItemCacheSearchTVFull>

    /**
     * @return a [PagingSource] for cached TopRated Movies.
     */
    fun getCachedTopRatedMovies(): PagingSource<Int, TitleItemCacheTopRatedMovieFull>

    /**
     * @return a [PagingSource] for cached TopRated TV.
     */
    fun getCachedTopRatedTV(): PagingSource<Int, TitleItemCacheTopRatedTVFull>

    /**
     * @return a [PagingSource] for cached Upcoming Movies.
     */
    fun getCachedUpcomingMovies(): PagingSource<Int, TitleItemCacheUpcomingMovieFull>

    /**
     * @return [Flow] of list of [RecentSearch] which represent what the user has searched for
     */
    fun getRecentSearches(): Flow<List<RecentSearch>>

    /**
     * Inserts a new searched string into the database. If the are already 100 searches stored, the
     * oldest search will be deleted as there is no need to store an infinite number of searches.
     */
    suspend fun saveNewRecentSearch(newSearch: String)
}

class TitlesLocalDataSourceImpl @Inject constructor(
    private val titlesDao: TitlesDao,
    private val discoverMoviesDao: DiscoverMovieCacheDao,
    private val discoverTVDao: DiscoverTVCacheDao,
    private val popularMoviesDao: PopularMoviesCacheDao,
    private val popularTVDao: PopularTVCacheDao,
    private val searchAllDao: SearchAllCacheDao,
    private val searchMoviesDao: SearchMoviesCacheDao,
    private val searchTVDao: SearchTVCacheDao,
    private val topRatedMoviesDao: TopRatedMoviesCacheDao,
    private val topRatedTVDao: TopRatedTVCacheDao,
    private val upcomingMoviesDao: UpcomingMoviesCacheDao,
    private val recentSearchesDao: RecentSearchesDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : TitlesLocalDataSource {

    override suspend fun bookmarkTitleItem(titleItemFull: TitleItemFull) = withContext(dispatcher) {

        // Check if title already exists first. If so - update only
        val titleItemExists = titlesDao.checkIfTitleItemExists(
            type = titleItemFull.type,
            mediaId = titleItemFull.mediaId
        )

        if (titleItemExists) {
            titlesDao.updateTitleItem(titleItemFull = titleItemFull)
        } else {
            /* Handling the logic in Dao because it should happen in a transaction to make sure it
            * both TitleItemEntity and GenreForTitleEntity get saved in the database. Currently
            * 'bookmarking' logic is to simply save a TitleItem  */
            titlesDao.insertTitleItem(titleItemFull = titleItemFull)
        }
    }

    override suspend fun unBookmarkTitleItem(titleItemFull: TitleItemFull) {
        // Just in case, check if title already exists first.
        val titleItemExists = titlesDao.checkIfTitleItemExists(
            type = titleItemFull.type,
            mediaId = titleItemFull.mediaId
        )

        // If exists - deleting.
        if (titleItemExists) {
            titlesDao.deleteTitleItem(titleItemFull)
        }
    }

    override suspend fun getAllBookmarkedTitles(): List<TitleItemFull>? = withContext(dispatcher) {
        val allTitleItemEntities = titlesDao.getAllTitleItems()
        return@withContext allTitleItemEntities?.toTitleItemsFull()
    }

    override fun allWatchlistedTitlesFlow(): Flow<List<TitleItemFull>> {
        return titlesDao.allWatchlistedTitleItems().map {
            it.toTitleItemsFull()
        }
    }

    override suspend fun checkIfTitleItemWatchlisted(titleItemFull: TitleItemFull): Boolean =
        withContext(dispatcher) {
            titlesDao.checkIfTitleItemExists(
                type = titleItemFull.type,
                mediaId = titleItemFull.mediaId
            )
        }

    override fun getCachedDiscoverMovies(): PagingSource<Int, TitleItemCacheDiscoverMovieFull> =
        discoverMoviesDao.getCachedTitles()

    override fun getCachedDiscoverTV(): PagingSource<Int, TitleItemCacheDiscoverTVFull> =
        discoverTVDao.getCachedTitles()

    override fun getCachedPopularMovies(): PagingSource<Int, TitleItemCachePopularMovieFull> =
        popularMoviesDao.getCachedTitles()

    override fun getCachedPopularTV(): PagingSource<Int, TitleItemCachePopularTVFull> =
        popularTVDao.getCachedTitles()

    override fun getCachedSearchAll(): PagingSource<Int, TitleItemCacheSearchAllFull> =
        searchAllDao.getCachedTitles()

    override fun getCachedSearchMovies(): PagingSource<Int, TitleItemCacheSearchMovieFull> =
        searchMoviesDao.getCachedTitles()

    override fun getCachedSearchTV(): PagingSource<Int, TitleItemCacheSearchTVFull> =
        searchTVDao.getCachedTitles()

    override fun getCachedTopRatedMovies(): PagingSource<Int, TitleItemCacheTopRatedMovieFull> =
        topRatedMoviesDao.getCachedTitles()

    override fun getCachedTopRatedTV(): PagingSource<Int, TitleItemCacheTopRatedTVFull> =
        topRatedTVDao.getCachedTitles()

    override fun getCachedUpcomingMovies(): PagingSource<Int, TitleItemCacheUpcomingMovieFull> =
        upcomingMoviesDao.getCachedTitles()

    override fun getRecentSearches(): Flow<List<RecentSearch>> =
        recentSearchesDao.getRecentSearches()
            .flowOn(dispatcher)
            .map { it.toRecentSearchList() }

    override suspend fun saveNewRecentSearch(newSearch: String) = withContext(dispatcher) {
        if (recentSearchesDao.getCountOfEntries() >= 100) {
            recentSearchesDao.deleteOldestRecentSearch()
        }
        recentSearchesDao.insertRecentSearch(
            RecentSearchEntity(
                searchedString = newSearch,
                searchedDateTime = LocalDateTime.now()
            )
        )
    }
}