package com.myapplications.mywatchlist.data.mediators

import androidx.paging.ExperimentalPagingApi
import androidx.paging.RemoteMediator
import com.myapplications.mywatchlist.data.entities.cached.*
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleListFilter

@OptIn(ExperimentalPagingApi::class)
interface TitlesRemoteMediatorProvider {
    fun getDiscoverMovieRemoteMediator(
        filter: TitleListFilter,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCacheDiscoverMovieFull>

    fun getDiscoverTVRemoteMediator(
        filter: TitleListFilter,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCacheDiscoverTVFull>

    fun getPopularMoviesRemoteMediator(
        filter: TitleListFilter,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCachePopularMovieFull>

    fun getPopularTVRemoteMediator(
        filter: TitleListFilter,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCachePopularTVFull>

    fun getSearchAllRemoteMediator(
        query: String,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCacheSearchAllFull>

    fun getSearchMoviesRemoteMediator(
        query: String,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCacheSearchMovieFull>

    fun getSearchTVRemoteMediator(
        query: String,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCacheSearchTVFull>

    fun getTopRatedMoviesRemoteMediator(
        filter: TitleListFilter,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCacheTopRatedMovieFull>

    fun getTopRatedTVRemoteMediator(
        filter: TitleListFilter,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCacheTopRatedTVFull>

    fun getUpcomingMoviesRemoteMediator(
        filter: TitleListFilter,
        genres: List<Genre>
    ): RemoteMediator<Int, TitleItemCacheUpcomingMovieFull>
}