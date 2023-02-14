package com.myapplications.mywatchlist.data.local.titles

import android.util.Log
import androidx.paging.PagingSource
import androidx.room.*
import com.myapplications.mywatchlist.data.entities.GenreForCacheItemTrending
import com.myapplications.mywatchlist.data.entities.RemoteKeyTrending
import com.myapplications.mywatchlist.data.entities.TitleItemCacheTrending
import com.myapplications.mywatchlist.data.entities.TitleItemCacheTrendingFull
import com.myapplications.mywatchlist.data.mappers.toGenreForCacheItemTrendingList
import com.myapplications.mywatchlist.data.mappers.toTitleItemCacheTrendingList
import com.myapplications.mywatchlist.domain.entities.TitleItemFull

@Dao
interface CacheDao {

    //#region TITLES TRENDING
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListTitleItemCacheTrending(titleItemCacheTrendingList: List<TitleItemCacheTrending>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenresForCachedTrending(genres: List<GenreForCacheItemTrending>)

    @Transaction
    suspend fun insertCachedTrendingItems(
        titlesList: List<TitleItemFull>,
        page: Int,
        prevKey: Int?,
        nextKey: Int?,
        createdOn: Long
    ) {
        // Inserts titles and get their ids
        val ids = insertListTitleItemCacheTrending(titlesList.toTitleItemCacheTrendingList(page = page))
        ids.forEachIndexed { index, l ->
            Log.d("CACHE_DAO", "ids after insert titles: id #$index:$l. For title ${titlesList[index].name}")
        }
        // Insert genres for each of inserted titles
        titlesList.forEachIndexed { index, title ->
            insertGenresForCachedTrending(
                genres = title.genres.toGenreForCacheItemTrendingList(ids[index])
            )
        }
        // Insert remote keys for each of inserted titles
        val remoteKeys = ids.map { id ->
            RemoteKeyTrending(
                cachedTitleId = id,
                prevKey = prevKey,
                currentPage = page,
                nextKey = nextKey,
                createdOn = createdOn
            )
        }
        insertRemoteKeysTrending(remoteKeysTrending = remoteKeys)
    }

    @Query("SELECT * FROM 'title_item_cache_trending' ORDER BY page")
    fun getTrendingTitles(): PagingSource<Int, TitleItemCacheTrendingFull>

    @Query(
        "SELECT * FROM 'title_item_cache_trending' " +
        "ORDER BY page, id"
    )
    fun getTrendingTitlesFiltered(): PagingSource<Int, TitleItemCacheTrendingFull>

    @Query("DELETE FROM 'title_item_cache_trending'")
    suspend fun clearAllTrendingTitles()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemoteKeysTrending(remoteKeysTrending: List<RemoteKeyTrending>)

    @Query("SELECT * FROM 'remote_key_trending' WHERE cachedTitleId = :id")
    suspend fun getRemoteKeyTrendingById(id: Long): RemoteKeyTrending?

    @Query("DELETE FROM remote_key_trending")
    suspend fun clearRemoteKeysTrending()

    @Query("SELECT createdOn FROM remote_key_trending ORDER BY createdOn DESC LIMIT 1")
    suspend fun getCreationTime(): Long?
    //#endregion
}