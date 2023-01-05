package com.myapplications.mywatchlist.data.repositories

import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.local.GenresLocalDataSource
import com.myapplications.mywatchlist.data.remote.GenresRemoteDataSource
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.result.BasicResult
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GenresRepositoryImpl @Inject constructor(
    private val remoteDataSource: GenresRemoteDataSource,
    private val localDataSource: GenresLocalDataSource,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : GenresRepository {

    override suspend fun updateGenresFromApi(): BasicResult = withContext(dispatcher) {
        when (val remoteResult = remoteDataSource.getAllGenresFromApi()) {
            is ResultOf.Failure -> BasicResult.Failure(
                exception = remoteResult.throwable
                    ?: Exception("Unknown Exception occurred fetching genres from the api")
            )
            is ResultOf.Success -> {
                val genres = remoteResult.data.genres
                if (genres.isNullOrEmpty()) {
                    // This shouldn't happen, but making a check just in case
                    return@withContext BasicResult.Failure(null)
                }
                localDataSource.saveGenresToDatabase(genres)
                return@withContext BasicResult.Success(null)
            }
        }
    }

    override suspend fun getAvailableGenres(): List<Genre> = withContext(dispatcher) {
        localDataSource.getAvailableGenres()
    }
}