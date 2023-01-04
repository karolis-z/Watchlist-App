package com.myapplications.mywatchlist.data.repositories

import com.myapplications.mywatchlist.core.result.ResultOf
import com.myapplications.mywatchlist.data.local.LocalDataSource
import com.myapplications.mywatchlist.data.remote.RemoteDataSource
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.repositories.TitlesRepository
import javax.inject.Inject

class TitlesRepositoryImpl @Inject constructor(
    localDataSource: LocalDataSource,
    remoteDataSource: RemoteDataSource
) : TitlesRepository {

    override suspend fun searchTitles(query: String): ResultOf<List<TitleItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun bookmarkTitle(title: TitleItem) {
        TODO("Not yet implemented")
    }
}