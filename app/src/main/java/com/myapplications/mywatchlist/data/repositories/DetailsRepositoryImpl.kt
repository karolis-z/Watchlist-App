package com.myapplications.mywatchlist.data.repositories

import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.core.util.NetworkStatusManager
import com.myapplications.mywatchlist.data.ApiGetDetailsException
import com.myapplications.mywatchlist.data.local.details.LocalDetailsDataSource
import com.myapplications.mywatchlist.data.remote.RemoteDetailsDataSource
import com.myapplications.mywatchlist.domain.entities.Title
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.domain.repositories.DetailsRepository
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "DETAILS_REPOSITORY"

class DetailsRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDetailsDataSource,
    private val localDataSource: LocalDetailsDataSource,
    private val genresRepository: GenresRepository,
    private val networkStatusManager: NetworkStatusManager,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : DetailsRepository {

    override suspend fun getTitle(mediaId: Long, type: TitleType): ResultOf<Title> =
        withContext(dispatcher) {
            // Firstly need to check if title already available in local db
            val localResult = localDataSource.getTitle(mediaId = mediaId, type = TitleType.MOVIE)
            when(localResult){
                is ResultOf.Failure -> {
                    val genresList = genresRepository.getAvailableGenres()
                    when (networkStatusManager.isOnline()) {
                        true -> {
                            val remoteResult = remoteDataSource.getTitle(
                                mediaId = mediaId,
                                type = type,
                                allGenres = genresList
                            )
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
                        false -> {
                            return@withContext ResultOf.Failure(
                                message = null,
                                throwable = ApiGetDetailsException.NoConnectionException(null, null)
                            )
                        }
                    }
                }
                is ResultOf.Success -> {
                    return@withContext ResultOf.Success(data = localResult.data)
                }
            }
        }

    override suspend fun bookmarkTitle(title: Title) = withContext(dispatcher) {
        localDataSource.bookmarkTitle(title)
    }

    override suspend fun unBookmarkTitle(title: Title) = withContext(dispatcher) {
        localDataSource.unBookmarkTitle(title)
    }
}