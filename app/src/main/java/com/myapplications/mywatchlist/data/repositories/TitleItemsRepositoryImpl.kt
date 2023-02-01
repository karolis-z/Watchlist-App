package com.myapplications.mywatchlist.data.repositories

import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.core.util.NetworkStatusManager
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.data.local.titles.TitlesLocalDataSource
import com.myapplications.mywatchlist.data.remote.TitlesRemoteDataSource
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.repositories.TitleItemsRepository
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "TITLES_REPOSITORY"

class TitleItemsRepositoryImpl @Inject constructor(
    private val localDataSource: TitlesLocalDataSource,
    private val remoteDataSource: TitlesRemoteDataSource,
    private val genresRepository: GenresRepository,
    private val networkStatusManager: NetworkStatusManager,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : TitleItemsRepository {

    override suspend fun searchTitles(query: String): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            return@withContext getTitleItemsFullResult(
                requestType = TitleItemsRequestType.SearchQuery(query)
            )
        }

    override suspend fun bookmarkTitleItem(titleItemFull: TitleItemFull) = withContext(dispatcher) {
        localDataSource.bookmarkTitleItem(titleItemFull = titleItemFull.copy(isWatchlisted = true))
    }

    override suspend fun unBookmarkTitleItem(titleItemFull: TitleItemFull) = withContext(dispatcher) {
        localDataSource.unBookmarkTitleItem(titleItemFull = titleItemFull)
    }

    override suspend fun getWatchlistedTitles(): List<TitleItemFull>? = withContext(dispatcher) {
        localDataSource.getAllBookmarkedTitles()
    }

    override fun allWatchlistedTitleItems(): Flow<List<TitleItemFull>> {
        return localDataSource.allWatchlistedTitlesFlow()
    }

    override suspend fun getTrendingTitles(): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        return@withContext getTitleItemsFullResult(
            requestType = TitleItemsRequestType.TrendingMoviesAndTV
        )
    }

    override suspend fun getPopularTitles(): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            return@withContext getTitleItemsFullResult(
                requestType = TitleItemsRequestType.PopularMoviesAndTV
            )
        }

    /**
     * General function to get a result from remote data source
     * @param requestType of type [TitleItemsRequestType] determines what type of request will be made
     */
    private suspend fun getTitleItemsFullResult(
        requestType: TitleItemsRequestType
    ): ResultOf<List<TitleItemFull>> = withContext(dispatcher) {
        if (!networkStatusManager.isOnline()){
            return@withContext ResultOf.Failure(
                message = null,
                throwable = ApiGetTitleItemsExceptions.NoConnectionException(null, null)
            )
        }
        val genresList = genresRepository.getAvailableGenres()
        val result = when (requestType) {
            TitleItemsRequestType.PopularMoviesAndTV ->
                remoteDataSource.getPopularTitles(allGenres = genresList)
            is TitleItemsRequestType.SearchQuery -> remoteDataSource.searchTitles(
                query = requestType.query,
                allGenres = genresList
            )
            TitleItemsRequestType.TrendingMoviesAndTV ->
                remoteDataSource.getTrendingTitles(allGenres = genresList)
        }
        return@withContext parseTitlesListResult(result)
    }

    /**
     * Parses the result fetched from Remote Data Source: if result is Success, then filters the
     * received list by exchanging titles with those in the local database (if those titles are the
     * same). This is so UI lists can show whether title items are marked as Watchlisted or not.
     * @param result is a [ResultOf] returned from remote data source.
     * @return [ResultOf] that can be further returned to the requester.
     */
    private suspend fun parseTitlesListResult(result: ResultOf<List<TitleItemFull>>): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            when (result) {
                is ResultOf.Failure -> return@withContext result
                is ResultOf.Success -> {
                    val titlesFilteredForWatchlisted = mutableListOf<TitleItemFull>()
                    result.data.forEach { titleItem ->
                        val isWatchlisted = localDataSource.checkIfTitleItemWatchlisted(titleItem)
                        if (isWatchlisted) {
                            titlesFilteredForWatchlisted.add(titleItem.copy(isWatchlisted = true))
                        } else {
                            titlesFilteredForWatchlisted.add(titleItem)
                        }
                    }
                    return@withContext ResultOf.Success(data = titlesFilteredForWatchlisted)
                }
            }
        }

    private sealed class TitleItemsRequestType {
        object TrendingMoviesAndTV : TitleItemsRequestType()
        object PopularMoviesAndTV : TitleItemsRequestType()
        data class SearchQuery(val query: String): TitleItemsRequestType()
    }
}