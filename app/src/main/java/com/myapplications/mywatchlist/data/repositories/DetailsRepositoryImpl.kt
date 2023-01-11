package com.myapplications.mywatchlist.data.repositories

import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.remote.RemoteDetailsDataSource
import com.myapplications.mywatchlist.domain.entities.Movie
import com.myapplications.mywatchlist.domain.entities.TV
import com.myapplications.mywatchlist.domain.repositories.DetailsRepository
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DetailsRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDetailsDataSource,
    private val genresRepository: GenresRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : DetailsRepository {

    override suspend fun getMovie(id: Long): ResultOf<Movie> = withContext(dispatcher) {
        val genresList = genresRepository.getAvailableGenres()
        remoteDataSource.getMovie(id = id, allGenres = genresList)
    }

    override suspend fun getTv(id: Long): ResultOf<TV> = withContext(dispatcher) {
        val genresList = genresRepository.getAvailableGenres()
        remoteDataSource.getTv(id = id, allGenres = genresList)
    }
}