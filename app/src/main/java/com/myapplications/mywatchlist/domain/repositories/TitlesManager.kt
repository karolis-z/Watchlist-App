package com.myapplications.mywatchlist.domain.repositories

import com.myapplications.mywatchlist.domain.entities.Title
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.flow.Flow

interface TitlesManager {
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

    /**
     * Retrieves a [Title]
     */
    suspend fun getTitle(mediaId: Long, type: TitleType): ResultOf<Title>

    /** Adds the [Title] to user's watchlist */
    suspend fun bookmarkTitle(title: Title)

    /** Removes the [Title] from user's watchlist */
    suspend fun unBookmarkTitle(title: Title)
}