package com.myapplications.mywatchlist.data.local.titles.cache

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.myapplications.mywatchlist.data.entities.cached.GenreForCacheItem
import com.myapplications.mywatchlist.data.entities.cached.RemoteKey
import com.myapplications.mywatchlist.data.entities.cached.TitleItemCache

interface BaseCacheDao<T: TitleItemCache, G: GenreForCacheItem, K: RemoteKey> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListTitleItemCache(titleItemCacheList: List<T>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenresForCache(genres: List<G>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemoteKeys(remoteKeys: List<K>)

}
