package com.myapplications.mywatchlist.domain.repositories

import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.result.BasicResult

interface GenresRepository {

    /**
     * Updates the local database with list of [Genre] retrieved from the api.
     * @return [BasicResult] indicating Success or Failure
     */
    suspend fun updateGenresFromApi(): BasicResult

    /**
     * @return a list of [Genre]
     */
    suspend fun getAvailableGenres(): List<Genre>

}