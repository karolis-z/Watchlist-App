package com.myapplications.mywatchlist.data.local.genres

import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.mappers.toGenreEntities
import com.myapplications.mywatchlist.data.mappers.toGenres
import com.myapplications.mywatchlist.domain.entities.Genre
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface GenresLocalDataSource {
    /**
     * Deletes from the local database existing list of [Genre] and updates with new provided
     * [genresList]
     * @param genresList the new full list of [Genre]
     */
    suspend fun saveGenresToDatabase(genresList: List<Genre>)

    /**
     * Retrieves the currently stored list of [Genre] from the local database.
     */
    suspend fun getAvailableGenres(): List<Genre>
}

class GenresLocalDataSourceImpl @Inject constructor(
    private val genresDao: GenresDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : GenresLocalDataSource {

    override suspend fun saveGenresToDatabase(genresList: List<Genre>) = withContext(dispatcher) {
        genresDao.deleteAllAndSaveNewGenres(genresList.toGenreEntities())
    }

    override suspend fun getAvailableGenres(): List<Genre> = withContext(dispatcher) {
        return@withContext genresDao.getAllGenres().toGenres()
    }
}