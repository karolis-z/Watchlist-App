package com.myapplications.mywatchlist.data.repositories

import com.myapplications.mywatchlist.data.local.TitlesLocalDataSource
import com.myapplications.mywatchlist.data.remote.TitlesRemoteDataSource
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.repositories.TitlesRepository
import com.myapplications.mywatchlist.domain.result.ResultOf
import javax.inject.Inject

class TitlesRepositoryImpl @Inject constructor(
    titlesLocalDataSource: TitlesLocalDataSource,
    titlesRemoteDataSource: TitlesRemoteDataSource
) : TitlesRepository {

    override suspend fun searchTitles(query: String): ResultOf<List<TitleItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun bookmarkTitle(title: TitleItem) {
        TODO("Not yet implemented")
    }
}