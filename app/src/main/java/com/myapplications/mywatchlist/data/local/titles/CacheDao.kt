package com.myapplications.mywatchlist.data.local.titles

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
    suspend fun insertListTitleItemCacheTrending(titleItemCacheTrendingList: List<TitleItemCacheTrending>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenresForCachedTrending(genres: List<GenreForCacheItemTrending>)

    @Transaction
    suspend fun insertCachedTrendingItems(titlesList: List<TitleItemFull>, page: Int) {
        // Inserts titles and get their ids
        insertListTitleItemCacheTrending(titlesList.toTitleItemCacheTrendingList(page = page))

        // Insert genres for each of insert titles
        titlesList.forEachIndexed { index, title ->
            insertGenresForCachedTrending(
                genres = title.genres.toGenreForCacheItemTrendingList(titlesList[index].id)
            )
        }
    }

    @Query("SELECT * FROM 'title_item_cache_trending' ORDER BY page")
    fun getTrendingTitles(): PagingSource<Int, TitleItemCacheTrendingFull>

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