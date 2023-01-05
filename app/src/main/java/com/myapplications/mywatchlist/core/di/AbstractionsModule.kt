package com.myapplications.mywatchlist.core.di

import com.myapplications.mywatchlist.data.local.GenresLocalDataSource
import com.myapplications.mywatchlist.data.local.GenresLocalDataSourceImpl
import com.myapplications.mywatchlist.data.local.TitlesLocalDataSource
import com.myapplications.mywatchlist.data.local.TitlesLocalDataSourceImpl
import com.myapplications.mywatchlist.data.remote.GenresRemoteDataSource
import com.myapplications.mywatchlist.data.remote.GenresRemoteDataSourceImpl
import com.myapplications.mywatchlist.data.remote.TitlesRemoteDataSource
import com.myapplications.mywatchlist.data.remote.TitlesRemoteDataSourceImpl
import com.myapplications.mywatchlist.data.repositories.GenresRepositoryImpl
import com.myapplications.mywatchlist.data.repositories.TitlesRepositoryImpl
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
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
    abstract fun bindTitlesLocalDataSource(titlesLocalDataSourceImpl: TitlesLocalDataSourceImpl): TitlesLocalDataSource

    @Binds
    abstract fun bindTitlesRemoteDataSource(titlesRemoteDataSourceImpl: TitlesRemoteDataSourceImpl): TitlesRemoteDataSource

    @Binds
    abstract fun bindGenresRepository(genresRepositoryImpl: GenresRepositoryImpl): GenresRepository

    @Binds
    abstract fun bindGenresRemoteDataSource(genresRemoteDataSourceImpl: GenresRemoteDataSourceImpl): GenresRemoteDataSource

    @Binds
    abstract fun  bindGenresLocalDataSource(genresLocalDataSourceImpl: GenresLocalDataSourceImpl): GenresLocalDataSource

}