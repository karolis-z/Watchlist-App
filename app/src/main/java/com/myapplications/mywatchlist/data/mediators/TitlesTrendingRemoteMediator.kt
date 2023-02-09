package com.myapplications.mywatchlist.data.mediators

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.myapplications.mywatchlist.core.util.Constants.CACHING_TIMEOUT
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.data.entities.RemoteKeyTrending
import com.myapplications.mywatchlist.data.entities.TitleItemCacheTrendingFull
import com.myapplications.mywatchlist.data.local.WatchlistDatabase
import com.myapplications.mywatchlist.domain.repositories.TitleItemsRepository
import com.myapplications.mywatchlist.domain.result.ResultOf
import java.time.Instant
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class TitlesTrendingRemoteMediator @Inject constructor(
    private val titleItemsRepository: TitleItemsRepository,
    private val db: WatchlistDatabase
) : RemoteMediator<Int, TitleItemCacheTrendingFull>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TitleItemCacheTrendingFull>
    ): MediatorResult {
//        delay(2000)
        val page: Int = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
        }

        val trendingResult = titleItemsRepository.getTrendingTitlesPaginated(page = page)
        val endOfPaginationReached = when (trendingResult) {
            is ResultOf.Failure -> {
                if (trendingResult.throwable !is ApiGetTitleItemsExceptions) {
                    return MediatorResult.Error(
                        Exception("Unknown Error getting page of data from the api")
                    )
                } else {
                    when (trendingResult.throwable) {
                        is ApiGetTitleItemsExceptions.FailedApiRequestException,
                        is ApiGetTitleItemsExceptions.NoConnectionException ->
                            return MediatorResult.Error(trendingResult.throwable)
                        is ApiGetTitleItemsExceptions.NothingFoundException -> true
                    }
                }
            }
            is ResultOf.Success -> false
        }
        val titleList = (trendingResult as ResultOf.Success).data

        db.withTransaction {
            if (loadType == LoadType.REFRESH) {
                db.cacheDao().clearRemoteKeysTrending()
                db.cacheDao().clearAllTrendingTitles()
            }
            val prevKey = if (page > 1) page - 1 else null
            val nextKey = if (endOfPaginationReached) null else page + 1
            val remoteKeys = titleList.map {
                RemoteKeyTrending(
                    cachedTitleId = it.id,
                    prevKey = prevKey,
                    currentPage = page,
                    nextKey = nextKey,
                    createdOn = Instant.now().toEpochMilli()
                )
            }

            db.cacheDao().insertRemoteKeysTrending(remoteKeysTrending = remoteKeys)
            db.cacheDao().insertCachedTrendingItems(titlesList = titleList, page = page)
        }
        return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
    }

    override suspend fun initialize(): InitializeAction {
        val timeDifference = Instant.now().toEpochMilli() - (db.cacheDao().getCreationTime() ?: 0)
        return if (timeDifference < CACHING_TIMEOUT) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, TitleItemCacheTrendingFull>
    ): RemoteKeyTrending? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.titleItem?.id?.let { id ->
                db.cacheDao().getRemoteKeyTrendingById(id)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(
        state: PagingState<Int, TitleItemCacheTrendingFull>
    ): RemoteKeyTrending? {
        return state.pages.firstOrNull {
            it.data.isNotEmpty()
        }?.data?.firstOrNull()?.let { titleItem ->
            db.cacheDao().getRemoteKeyTrendingById(titleItem.titleItem.id)
        }
    }

    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, TitleItemCacheTrendingFull>
    ): RemoteKeyTrending? {
        return state.pages.lastOrNull {
            it.data.isNotEmpty()
        }?.data?.lastOrNull()?.let { titleItem ->
            db.cacheDao().getRemoteKeyTrendingById(titleItem.titleItem.id)
        }
    }

}