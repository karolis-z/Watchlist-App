package com.myapplications.mywatchlist.data.repositories

import android.util.Log
import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.local.titles.TitlesLocalDataSource
import com.myapplications.mywatchlist.data.mappers.toTitleItem
import com.myapplications.mywatchlist.data.remote.TitlesRemoteDataSource
import com.myapplications.mywatchlist.domain.entities.Movie
import com.myapplications.mywatchlist.domain.entities.TV
import com.myapplications.mywatchlist.domain.entities.Title
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.repositories.TitlesRepository
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "TITLES_REPOSITORY"

class TitlesRepositoryImpl @Inject constructor(
    private val localDataSource: TitlesLocalDataSource,
    private val remoteDataSource: TitlesRemoteDataSource,
    private val genresRepository: GenresRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : TitlesRepository {

    // TODO: Add check for internet connection available and return appropriate result
    override suspend fun searchTitles(query: String): ResultOf<List<TitleItem>> =
        withContext(dispatcher) {
            val genresList = genresRepository.getAvailableGenres()
            val result = remoteDataSource.searchTitles(query = query, allGenres = genresList)
            return@withContext parseTitlesListResult(result)
        }

    override suspend fun bookmarkTitleItem(titleItem: TitleItem) = withContext(dispatcher) {
        localDataSource.bookmarkTitleItem(titleItem = titleItem.copy(isWatchlisted = true))
    }

    override suspend fun unBookmarkTitleItem(titleItem: TitleItem) = withContext(dispatcher) {
        localDataSource.unBookmarkTitleItem(titleItem = titleItem)
    }

    override suspend fun bookmarkTitle(title: Title) = withContext(dispatcher) {
        val titleItem = when (title){
            is Movie -> title.toTitleItem()
            is TV -> title.toTitleItem()
            else -> null
        }
        if (titleItem == null) {
            Log.e(TAG, "bookmarkTitle: the provided title was neither Movie nor TV type. " +
                    "Unable to bookmark this title: $title")
        } else {
            bookmarkTitleItem(titleItem = titleItem)
            return@withContext
        }
    }

    override suspend fun unBookmarkTitle(title: Title) = withContext(dispatcher) {
        val titleItem = when (title){
            is Movie -> title.toTitleItem()
            is TV -> title.toTitleItem()
            else -> null
        }
        if (titleItem == null) {
            Log.e(TAG, "unBookmarkTitle: the provided title was neither Movie nor TV type. " +
                    "Unable to unbookmark this title: $title")
        } else {
            unBookmarkTitleItem(titleItem = titleItem)
            return@withContext
        }
    }

    override suspend fun getWatchlistedTitles(): List<TitleItem>? = withContext(dispatcher) {
        localDataSource.getAllBookmarkedTitles()
    }

    override fun allWatchlistedTitleItems(): Flow<List<TitleItem>> {
        return localDataSource.allWatchlistedTitlesFlow()
    }

    // TODO: Add check for internet connection available and return appropriate result
    override suspend fun getTrendingTitles(): ResultOf<List<TitleItem>> = withContext(dispatcher) {
        val genresList = genresRepository.getAvailableGenres()
        val result = remoteDataSource.getTrendingTitles(allGenres = genresList)
        return@withContext parseTitlesListResult(result)
    }

    /**
     * Parses the result fetched from Remote Data Source: if result is Success, then filters the
     * received list by exchanging titles with those in the local database (if those titles are the
     * same). This is so UI lists can show whether title items are marked as Watchlisted or not.
     * @param result is a [ResultOf] returned from remote data source.
     * @return [ResultOf] that can be further returned to the requester.
     */
    private suspend fun parseTitlesListResult(result: ResultOf<List<TitleItem>>): ResultOf<List<TitleItem>> =
        withContext(dispatcher) {
            when (result) {
                is ResultOf.Failure -> return@withContext result
                is ResultOf.Success -> {
                    val titlesFilteredForWatchlisted = mutableListOf<TitleItem>()
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
}