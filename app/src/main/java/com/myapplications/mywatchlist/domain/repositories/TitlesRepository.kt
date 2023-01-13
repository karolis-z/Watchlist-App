package com.myapplications.mywatchlist.domain.repositories

import com.myapplications.mywatchlist.domain.entities.Title
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.flow.Flow

interface TitlesRepository {

    /**
     * Searches for the given query in The Movie Database.
     * @return [ResultOf.Success] containing List of [TitleItem] if successful and [ResultOf.Failure]
     * if not.
     */
    suspend fun searchTitles(query: String): ResultOf<List<TitleItem>>

    /**
     * Bookmarks the [TitleItem] as added to user's watchlist.
     */
    suspend fun bookmarkTitleItem(titleItem: TitleItem)

    /**
     * Unbookmarks the [TitleItem] so it's not longer represented in the user's watchlist.
     */
    suspend fun unBookmarkTitleItem(titleItem: TitleItem)

    /** Bookmarks the [Title] by converting it to [TitleItem] so it's visible in the user's watchlist */
    suspend fun bookmarkTitle(title: Title)

    /** Unbookmarks the [Title] by removing the associated [TitleItem] from the user's watchlist */
    suspend fun unBookmarkTitle(title: Title)

    /**
     * Returns titles stored in local database.
     * @return list of [TitleItem] or null if no [TitleItem]s are stored.
     */
    suspend fun getWatchlistedTitles(): List<TitleItem>?

    /**
     * @return a [Flow] of list of [TitleItem]s that are watchlisted.
     */
    fun allWatchlistedTitleItems(): Flow<List<TitleItem>>

    /**
     * @return a list of [TitleItem]s that are trending
     */
    suspend fun getTrendingTitles(): ResultOf<List<TitleItem>>
}