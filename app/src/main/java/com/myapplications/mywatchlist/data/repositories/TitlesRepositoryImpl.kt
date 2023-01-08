package com.myapplications.mywatchlist.data.repositories

import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.local.titles.TitlesLocalDataSource
import com.myapplications.mywatchlist.data.remote.TitlesRemoteDataSource
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.repositories.TitlesRepository
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

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
            when(result){
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

    override suspend fun bookmarkTitle(titleItem: TitleItem) = withContext(dispatcher) {
        localDataSource.bookmarkTitleItem(titleItem = titleItem.copy(isWatchlisted = true))
    }

    override suspend fun unBookmarkTitle(titleItem: TitleItem) = withContext(dispatcher) {
        localDataSource.unBookmarkTitleItem(titleItem = titleItem)
    }

    override suspend fun getWatchlistedTitles(): List<TitleItem>? = withContext(dispatcher) {
        localDataSource.getAllBookmarkedTitles()
    }

    override fun allWatchlistedTitleItems(): Flow<List<TitleItem>> {
        return localDataSource.allWatchlistedTitlesFlow()
    }
}