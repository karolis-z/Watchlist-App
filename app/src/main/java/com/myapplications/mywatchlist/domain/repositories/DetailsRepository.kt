package com.myapplications.mywatchlist.domain.repositories

import com.myapplications.mywatchlist.domain.entities.Movie
import com.myapplications.mywatchlist.domain.entities.TV
import com.myapplications.mywatchlist.domain.entities.Title
import com.myapplications.mywatchlist.domain.result.ResultOf

interface DetailsRepository {

    /** Retrieves a [Movie] */
    suspend fun getMovie(id: Long): ResultOf<Movie>

    /** Retrieves a [TV] */
    suspend fun getTv(id: Long): ResultOf<TV>

    /** Adds the [Title] to user's watchlist */
    suspend fun bookmarkTitle(title: Title)

    /** Removes the [Title] from user's watchlist */
    suspend fun unBookmarkTitle(title: Title)
}