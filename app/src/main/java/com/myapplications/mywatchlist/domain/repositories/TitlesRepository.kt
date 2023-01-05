package com.myapplications.mywatchlist.domain.repositories

import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.result.ResultOf

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
    suspend fun bookmarkTitle(title: TitleItem)
}