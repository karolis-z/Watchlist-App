package com.myapplications.mywatchlist.data.repositories

import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.local.TitlesLocalDataSource
import com.myapplications.mywatchlist.data.remote.TitlesRemoteDataSource
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.repositories.TitlesRepository
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TitlesRepositoryImpl @Inject constructor(
    private val localDataSource: TitlesLocalDataSource,
    private val remoteDataSource: TitlesRemoteDataSource,
    private val genresRepository: GenresRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : TitlesRepository {

    override suspend fun searchTitles(query: String): ResultOf<List<TitleItem>> =
        withContext(dispatcher) {
            val genresList = genresRepository.getAvailableGenres()
            remoteDataSource.searchTitles(query = query, allGenres = genresList)
        }

    override suspend fun bookmarkTitle(titleItem: TitleItem) {
        localDataSource.bookmarkTitleItem(titleItem = titleItem)
    }
}