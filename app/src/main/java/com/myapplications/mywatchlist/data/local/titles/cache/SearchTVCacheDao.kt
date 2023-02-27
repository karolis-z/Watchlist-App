package com.myapplications.mywatchlist.data.local.titles.cache

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.myapplications.mywatchlist.data.entities.cached.*
import com.myapplications.mywatchlist.data.mappers.toGenreForCacheItemList
import com.myapplications.mywatchlist.data.mappers.toTitleItemCacheList
import com.myapplications.mywatchlist.domain.entities.TitleItemFull

@Dao
abstract class SearchTVCacheDao :
    BaseCacheDao<TitleItemCacheSearchTV, GenreForCacheItemSearchTV, RemoteKeySearchTV> {

    @Transaction
    open suspend fun insertCachedTrendingItems(
        titlesList: List<TitleItemFull>,
        page: Int,
        prevKey: Int?,
        nextKey: Int?,
        createdOn: Long
    ) {
        // Inserts titles and get their ids
        val ids = insertListTitleItemCache(
            titleItemCacheList = titlesList.toTitleItemCacheList(page = page)
        )

        // Insert genres for each of inserted titles
        titlesList.forEachIndexed { index, title ->
            insertGenresForCache(
                genres = title.genres.toGenreForCacheItemList(ids[index])
            )
        }
        // Insert remote keys for each of inserted titles
        val remoteKeys = ids.map { id ->
            RemoteKeySearchTV(
                cachedTitleId = id,
                prevKey = prevKey,
                currentPage = page,
                nextKey = nextKey,
                createdOn = createdOn
            )
        }
        insertRemoteKeys(remoteKeys = remoteKeys)
    }

    @Query("SELECT * FROM 'title_item_cache_search_tv' ORDER BY page, id")
    abstract fun getCachedTitles(): PagingSource<Int, TitleItemCacheSearchTVFull>

    @Query("DELETE FROM 'title_item_cache_search_tv'")
    abstract suspend fun clearAllCachedTitles()

    @Query("SELECT * FROM 'remote_key_search_tv' WHERE cachedTitleId = :id")
    abstract suspend fun getRemoteKeyById(id: Long): RemoteKeySearchTV?

    @Query("DELETE FROM 'remote_key_search_tv'")
    abstract suspend fun clearRemoteKeys()

    @Query("SELECT createdOn FROM 'remote_key_search_tv' ORDER BY createdOn DESC LIMIT 1")
    abstract suspend fun getCreationTime(): Long?

}