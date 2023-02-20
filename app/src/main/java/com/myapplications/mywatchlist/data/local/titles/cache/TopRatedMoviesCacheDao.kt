package com.myapplications.mywatchlist.data.local.titles.cache

import android.util.Log
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.myapplications.mywatchlist.data.entities.cached.*
import com.myapplications.mywatchlist.data.mappers.toGenreForCacheItemList
import com.myapplications.mywatchlist.data.mappers.toTitleItemCacheList
import com.myapplications.mywatchlist.domain.entities.TitleItemFull

@Dao
abstract class TopRatedMoviesCacheDao :
    BaseCacheDao<TitleItemCacheTopRatedMovie, GenreForCacheItemTopRatedMovie, RemoteKeyTopRatedMovie> {

    @Transaction
    suspend fun insertCachedTrendingItems(
        titlesList: List<TitleItemFull>,
        page: Int,
        prevKey: Int?,
        nextKey: Int?,
        createdOn: Long
    ) {
        // Inserts titles and get their ids
        // TODO: NEED A CHECK IF THIS ACTUALLY WORKS
        val ids =
            insertListTitleItemCache(titleItemCacheList = titlesList.toTitleItemCacheList(page = page))
        ids.forEachIndexed { index, l ->
            Log.d(
                "CACHE_DAO",
                "ids after insert titles: id #$index:$l. For title ${titlesList[index].name}"
            )
        }
        // Insert genres for each of inserted titles
        titlesList.forEachIndexed { index, title ->
            insertGenresForCache(
                genres = title.genres.toGenreForCacheItemList(ids[index])
            )
        }
        // Insert remote keys for each of inserted titles
        val remoteKeys = ids.map { id ->
            RemoteKeyTopRatedMovie(
                cachedTitleId = id,
                prevKey = prevKey,
                currentPage = page,
                nextKey = nextKey,
                createdOn = createdOn
            )
        }
        insertRemoteKeys(remoteKeys = remoteKeys)
    }

    @Query("SELECT * FROM 'title_item_cache_toprated_movie' ORDER BY page, id")
    abstract fun getCachedTitles(): PagingSource<Int, TitleItemCacheTopRatedMovieFull>

    @Query("DELETE FROM 'title_item_cache_toprated_movie'")
    abstract suspend fun clearAllCachedTitles()

    @Query("SELECT * FROM 'remote_key_toprated_movie' WHERE cachedTitleId = :id")
    abstract suspend fun getRemoteKeyById(id: Long): RemoteKeyTopRatedMovie?

    @Query("DELETE FROM 'remote_key_toprated_movie'")
    abstract suspend fun clearRemoteKeys()

    @Query("SELECT createdOn FROM 'remote_key_toprated_movie' ORDER BY createdOn DESC LIMIT 1")
    abstract suspend fun getCreationTime(): Long?

}