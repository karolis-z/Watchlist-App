package com.myapplications.mywatchlist.domain.repositories

import com.myapplications.mywatchlist.domain.entities.Title
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.flow.Flow

interface TitlesManager {
    /**
     * Searches for the given query in The Movie Database.
     * @return [ResultOf.Success] containing List of [TitleItemFull] if successful and [ResultOf.Failure]
     * if not.
     */
    suspend fun searchTitles(query: String): ResultOf<List<TitleItemFull>>

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
     * Retrieves a [Title]
     */
    suspend fun getTitle(mediaId: Long, type: TitleType): ResultOf<Title>

    /** Adds the [Title] to user's watchlist */
    suspend fun bookmarkTitle(title: Title)

    /** Removes the [Title] from user's watchlist */
    suspend fun unBookmarkTitle(title: Title)
}