package com.myapplications.mywatchlist.data.repositories

import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.local.details.LocalDetailsDataSource
import com.myapplications.mywatchlist.data.remote.RemoteDetailsDataSource
import com.myapplications.mywatchlist.domain.entities.Movie
import com.myapplications.mywatchlist.domain.entities.TV
import com.myapplications.mywatchlist.domain.entities.Title
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.domain.repositories.DetailsRepository
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.repositories.TitlesRepository
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DetailsRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDetailsDataSource,
    private val localDataSource: LocalDetailsDataSource,
    private val genresRepository: GenresRepository,
    private val titlesRepository: TitlesRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : DetailsRepository {

    // TODO: Merge large with getTV
    override suspend fun getMovie(id: Long): ResultOf<Movie> = withContext(dispatcher) {
        // Firstly need to check if title already available in local db
        val localResult = localDataSource.getTitle(titleId = id, titleType =TitleType.MOVIE)
        when(localResult){
            is ResultOf.Failure -> {
                val genresList = genresRepository.getAvailableGenres()
                val remoteResult = remoteDataSource.getMovie(id = id, allGenres = genresList)
                when(remoteResult){
                    is ResultOf.Failure -> {
                        return@withContext ResultOf.Failure(
                            message = remoteResult.message,
                            throwable = remoteResult.throwable
                        )
                    }
                    is ResultOf.Success -> {
                        return@withContext ResultOf.Success(data = remoteResult.data)
                    }
                }
            }
            is ResultOf.Success -> {
                /* Casting to Movie safely because in this method we ensured we're getting
                a Movie local DB */
                return@withContext ResultOf.Success(data = localResult.data as Movie)
            }
        }
    }

    // TODO: Merge large with getMovie
    override suspend fun getTv(id: Long): ResultOf<TV> = withContext(dispatcher) {
        // Firstly need to check if title already available in local db
        val localResult = localDataSource.getTitle(titleId = id, titleType =TitleType.TV)
        when(localResult){
            is ResultOf.Failure -> {
                val genresList = genresRepository.getAvailableGenres()
                val remoteResult = remoteDataSource.getTv(id = id, allGenres = genresList)
                when(remoteResult){
                    is ResultOf.Failure -> {
                        return@withContext ResultOf.Failure(
                            message = remoteResult.message,
                            throwable = remoteResult.throwable
                        )
                    }
                    is ResultOf.Success -> {
                        return@withContext ResultOf.Success(data = remoteResult.data)
                    }
                }
            }
            is ResultOf.Success -> {
                /* Casting to TV safely because in this method we ensured we're getting
                a TV local DB */
                return@withContext ResultOf.Success(data = localResult.data as TV)
            }
        }
    }

    override suspend fun bookmarkTitle(title: Title) = withContext(dispatcher) {
        // Watchlist TitleItem
        titlesRepository.bookmarkTitle(title)

        // Watchlist Title
        localDataSource.bookmarkTitle(title)
    }

    override suspend fun unBookmarkTitle(title: Title) = withContext(dispatcher) {
        // Unbookmark the associates TitleItem
        titlesRepository.unBookmarkTitle(title)

        // Unbookmark the Title
        localDataSource.unBookmarkTitle(title)
    }
}