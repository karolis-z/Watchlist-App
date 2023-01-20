package com.myapplications.mywatchlist.data.repositories

import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.core.util.NetworkStatusManager
import com.myapplications.mywatchlist.data.ApiGetGenresExceptions
import com.myapplications.mywatchlist.data.datastore.UserPrefsRepository
import com.myapplications.mywatchlist.data.local.genres.GenresLocalDataSource
import com.myapplications.mywatchlist.data.remote.GenresRemoteDataSource
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.result.BasicResult
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

private const val TAG = "GENRES_REPOSITORY"

class GenresRepositoryImpl @Inject constructor(
    private val remoteDataSource: GenresRemoteDataSource,
    private val localDataSource: GenresLocalDataSource,
    private val networkStatusManager: NetworkStatusManager,
    private val userPrefsRepository: UserPrefsRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : GenresRepository {

    override suspend fun updateGenresFromApi(): BasicResult = withContext(dispatcher) {
        
        val genresLastUpdate = userPrefsRepository.getGenresLastUpdateDate()
        if (genresLastUpdate != null && genresLastUpdate == LocalDate.now()) {
            return@withContext BasicResult.Success(null)
        }

        if (!networkStatusManager.isOnline()) {
            return@withContext BasicResult.Failure(ApiGetGenresExceptions.NoConnectionException(null, null))
        }
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
                userPrefsRepository.updateGenresUpdateDate(LocalDate.now())
                return@withContext BasicResult.Success(null)
            }
        }
    }

    override suspend fun getAvailableGenres(): List<Genre> = withContext(dispatcher) {
        localDataSource.getAvailableGenres()
    }
}