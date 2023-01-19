package com.myapplications.mywatchlist.domain.repositories

import com.myapplications.mywatchlist.domain.entities.Title
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.domain.result.ResultOf

interface DetailsRepository {

    /**
     * Retrieves a [Title] of given [TitleType]
     */
    suspend fun getTitle(mediaId: Long, type: TitleType): ResultOf<Title>

    /** Adds the [Title] to user's watchlist */
    suspend fun bookmarkTitle(title: Title)

    /** Removes the [Title] from user's watchlist */
    suspend fun unBookmarkTitle(title: Title)
}