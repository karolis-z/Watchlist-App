package com.myapplications.mywatchlist.domain.repositories

import androidx.paging.PagingData
import com.myapplications.mywatchlist.domain.entities.RecentSearch
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.entities.TitleListFilter
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.flow.Flow

interface TitleItemsRepository {

    /**
     * Bookmarks the [TitleItemFull] as added to user's watchlist.
     */
    suspend fun bookmarkTitleItem(titleItemFull: TitleItemFull)

    /**
     * Unbookmarks the [TitleItemFull] so it's not longer represented in the user's watchlist.
     */
    suspend fun unBookmarkTitleItem(titleItemFull: TitleItemFull)

    /**
     * Returns titles stored in local database.
     * @return list of [TitleItemFull] or null if no [TitleItemFull]s are stored.
     */
    suspend fun getWatchlistedTitles(): List<TitleItemFull>?

    /**
     * @return a [Flow] of list of [TitleItemFull]s that are watchlisted.
     */
    fun allWatchlistedTitleItems(): Flow<List<TitleItemFull>>

    /**
     * @return a list of [TitleItemFull]s that are trending
     */
    suspend fun getTrendingTitles(): ResultOf<List<TitleItemFull>>

    /**
     * @return a [Flow] of [PagingData] of [TitleItemFull] of Movies that are Discovered using the
     * custom provided filter.
     * @param filter the [TitleListFilter] that should be applied to the query.
     */
    suspend fun getDiscoverMoviesPaginated(filter: TitleListFilter): Flow<PagingData<TitleItemFull>>

    /**
     * @return a [Flow] of [PagingData] of [TitleItemFull] of TV Shows that are Discovered using the
     * custom provided filter.
     * @param filter the [TitleListFilter] that should be applied to the query.
     */
    suspend fun getDiscoverTVPaginated(filter: TitleListFilter): Flow<PagingData<TitleItemFull>>

    /**
     * @return a list of Movie [TitleItemFull]s that are popular
     */
    suspend fun getPopularMovies(): ResultOf<List<TitleItemFull>>

    /**
     * @return a [Flow] of [PagingData] of [TitleItemFull] of Movies that are popular.
     * @param filter the [TitleListFilter] that should be applied to the query.
     */
    suspend fun getPopularMoviesPaginated(filter: TitleListFilter): Flow<PagingData<TitleItemFull>>

    /**
     * @return a list of TV Show [TitleItemFull]s that are popular
     */
    suspend fun getPopularTV(): ResultOf<List<TitleItemFull>>

    /**
     * @return a [Flow] of [PagingData] of [TitleItemFull] of TV Shows that are popular.
     * @param filter the [TitleListFilter] that should be applied to the query.
     */
    suspend fun getPopularTvPaginated(filter: TitleListFilter): Flow<PagingData<TitleItemFull>>

    /**
     * Searches for the given query in The Movie Database among Movies and TV Shows.
     * @return a [Flow] of [PagingData] of [TitleItemFull] of Titles matching the given [query]
     * @param query [String] to be searched
     */
    suspend fun searchAllPaginated(query: String): Flow<PagingData<TitleItemFull>>

    /**
     * Searches for the given query in The Movie Database among Movies.
     * @return a [Flow] of [PagingData] of [TitleItemFull] of Movies matching the given [query]
     * @param query [String] to be searched
     */
    suspend fun searchMoviesPaginated(query: String): Flow<PagingData<TitleItemFull>>

    /**
     * Searches for the given query in The Movie Database among TV Shows.
     * @return a [Flow] of [PagingData] of [TitleItemFull] of TV Shows matching the given [query]
     * @param query [String] to be searched
     */
    suspend fun searchTVPaginated(query: String): Flow<PagingData<TitleItemFull>>

    /**
     * @return a list of Movie [TitleItemFull]s that are top rated
     */
    suspend fun getTopRatedMovies(): ResultOf<List<TitleItemFull>>

    /**
     * @return a [Flow] of [PagingData] of [TitleItemFull] of Movies that are top rated.
     * @param filter the [TitleListFilter] that should be applied to the query.
     */
    suspend fun getTopRatedMoviesPaginated(filter: TitleListFilter): Flow<PagingData<TitleItemFull>>

    /**
     * @return a list of TV Show [TitleItemFull]s that are top rated
     */
    suspend fun getTopRatedTV(): ResultOf<List<TitleItemFull>>

    /**
     * @return a [Flow] of [PagingData] of [TitleItemFull] of TV Shows that are top rated.
     * @param filter the [TitleListFilter] that should be applied to the query.
     */
    suspend fun getTopRatedTVPaginated(filter: TitleListFilter): Flow<PagingData<TitleItemFull>>

    /**
     * @return a list of [TitleItemFull]s that are upcoming movies
     */
    suspend fun getUpcomingMovies(): ResultOf<List<TitleItemFull>>

    /**
     * @return a [Flow] of [PagingData] of [TitleItemFull] of Movies that are upcoming.
     * @param filter the [TitleListFilter] that should be applied to the query.
     */
    suspend fun getUpcomingMoviesPaginated(filter: TitleListFilter): Flow<PagingData<TitleItemFull>>


    /**
     * @return [Flow] of list of [RecentSearch] which represent what the user has searched for
     */
    fun getRecentSearches(): Flow<List<RecentSearch>>

    /**
     * Inserts a new searched string into the database.
     */
    suspend fun saveNewRecentSearch(newSearch: String)
}