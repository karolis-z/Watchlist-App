package com.myapplications.mywatchlist.domain.repositories

import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.entities.TitleListFilter
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.flow.Flow

interface TitleItemsRepository {

    /**
     * Searches for the given query in The Movie Database among Movies and TV Shows.
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * if not.
     * @param query [String] to be searched
     */
    suspend fun searchAll(query: String, page: Int): ResultOf<List<TitleItemFull>>

    /**
     * Searches for the given query in The Movie Database among Movies.
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * if not.
     * @param query [String] to be searched
     */
    suspend fun searchMovies(query: String, page: Int): ResultOf<List<TitleItemFull>>

    /**
     * Searches for the given query in The Movie Database among TV Shows.
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * if not.
     * @param query [String] to be searched
     */
    suspend fun searchTV(query: String, page: Int): ResultOf<List<TitleItemFull>>

    /**
     * Bookmarks the [TitleItemFull] as added to user's watchlist.
     */
    suspend fun bookmarkTitleItem(titleItemFull: TitleItemFull)

    /**
     * Unbookmarks the [TitleItemFull] so it's not longer represented in the user's watchlist.
     */
    suspend fun unBookmarkTitleItem(titleItemFull: TitleItemFull)

//    /** Bookmarks the [Title] by converting it to [TitleItem] so it's visible in the user's watchlist */
//    suspend fun bookmarkTitle(title: Title)
//
//    /** Unbookmarks the [Title] by removing the associated [TitleItem] from the user's watchlist */
//    suspend fun unBookmarkTitle(title: Title)

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
     * @return a list of Movie [TitleItemFull]s that are popular
     */
    suspend fun getPopularMovies(page: Int, filter: TitleListFilter): ResultOf<List<TitleItemFull>>

    /**
     * @return a list of TV Show [TitleItemFull]s that are popular
     */
    suspend fun getPopularTV(page: Int, filter: TitleListFilter): ResultOf<List<TitleItemFull>>

    /**
     * @return a list of Movie [TitleItemFull]s that are top rated
     */
    suspend fun getTopRatedMovies(page: Int, filter: TitleListFilter): ResultOf<List<TitleItemFull>>

    /**
     * @return a list of TV Show [TitleItemFull]s that are top rated
     */
    suspend fun getTopRatedTV(page: Int, filter: TitleListFilter): ResultOf<List<TitleItemFull>>

    /**
     * @return a list of [TitleItemFull]s that are upcoming movies
     */
    suspend fun getUpcomingMovies(page: Int, filter: TitleListFilter): ResultOf<List<TitleItemFull>>

    /**
     * @return a list of Movie [TitleItemFull]s that searched by the custom filter
     */
    suspend fun getDiscoverMovies(page: Int, filter: TitleListFilter): ResultOf<List<TitleItemFull>>

    /**
     * @return a list of TV Show [TitleItemFull]s that searched by the custom filter
     */
    suspend fun getDiscoverTV(page: Int, filter: TitleListFilter): ResultOf<List<TitleItemFull>>

}