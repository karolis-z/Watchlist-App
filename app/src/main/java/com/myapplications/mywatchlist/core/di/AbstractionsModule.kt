package com.myapplications.mywatchlist.core.di

import com.myapplications.mywatchlist.data.local.LocalDataSource
import com.myapplications.mywatchlist.data.local.LocalDataSourceImpl
import com.myapplications.mywatchlist.data.remote.RemoteDataSource
import com.myapplications.mywatchlist.data.remote.RemoteDataSourceImpl
import com.myapplications.mywatchlist.data.repositories.TitlesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AbstractionsModule {

    @Binds
    abstract fun bindTitlesRepository(titlesRepositoryImpl: TitlesRepositoryImpl): TitlesRepositoryImpl

    @Binds
    abstract fun bindLocalDataSource(localDataSourceImpl: LocalDataSourceImpl): LocalDataSource

    @Binds
    abstract fun bindRemoteDataSource(remoteDataSourceImpl: RemoteDataSourceImpl): RemoteDataSource

}