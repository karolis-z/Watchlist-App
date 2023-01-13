package com.myapplications.mywatchlist.core.di

import com.myapplications.mywatchlist.data.local.details.LocalDetailsDataSource
import com.myapplications.mywatchlist.data.local.details.LocalDetailsDataSourceImpl
import com.myapplications.mywatchlist.data.local.genres.GenresLocalDataSource
import com.myapplications.mywatchlist.data.local.genres.GenresLocalDataSourceImpl
import com.myapplications.mywatchlist.data.local.titles.TitlesLocalDataSource
import com.myapplications.mywatchlist.data.local.titles.TitlesLocalDataSourceImpl
import com.myapplications.mywatchlist.data.remote.*
import com.myapplications.mywatchlist.data.repositories.DetailsRepositoryImpl
import com.myapplications.mywatchlist.data.repositories.GenresRepositoryImpl
import com.myapplications.mywatchlist.data.repositories.TitleItemsRepositoryImpl
import com.myapplications.mywatchlist.data.repositories.TitlesManagerImpl
import com.myapplications.mywatchlist.domain.repositories.DetailsRepository
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.repositories.TitleItemsRepository
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AbstractionsModule {

    @Binds
    abstract fun bindTitlesRepository(titlesRepositoryImpl: TitleItemsRepositoryImpl): TitleItemsRepository

    @Binds
    abstract fun bindTitlesLocalDataSource(titlesLocalDataSourceImpl: TitlesLocalDataSourceImpl): TitlesLocalDataSource

    @Binds
    abstract fun bindTitlesRemoteDataSource(titlesRemoteDataSourceImpl: TitlesRemoteDataSourceImpl): TitlesRemoteDataSource

    @Binds
    abstract fun bindGenresRepository(genresRepositoryImpl: GenresRepositoryImpl): GenresRepository

    @Binds
    abstract fun bindGenresRemoteDataSource(genresRemoteDataSourceImpl: GenresRemoteDataSourceImpl): GenresRemoteDataSource

    @Binds
    abstract fun bindGenresLocalDataSource(genresLocalDataSourceImpl: GenresLocalDataSourceImpl): GenresLocalDataSource

    @Binds
    abstract fun bindDetailsRepository(detailsRepositoryImpl: DetailsRepositoryImpl): DetailsRepository

    @Binds
    abstract fun bindDetailsRemoteDataSource(detailsDataSourceImpl: RemoteDetailsDataSourceImpl): RemoteDetailsDataSource

    @Binds
    abstract fun bindDetailsLocalDataSource(detailsDataSourceImpl: LocalDetailsDataSourceImpl): LocalDetailsDataSource

    @Binds
    abstract fun bindTitlesManager(titlesManagerImpl: TitlesManagerImpl): TitlesManager

}