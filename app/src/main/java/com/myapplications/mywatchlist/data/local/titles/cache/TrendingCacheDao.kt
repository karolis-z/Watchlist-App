package com.myapplications.mywatchlist.data.local.titles.cache

import androidx.room.*
import com.myapplications.mywatchlist.data.entities.GenreForCacheItemTrending
import com.myapplications.mywatchlist.data.entities.TitleItemCacheTrending
import com.myapplications.mywatchlist.data.entities.TitleItemCacheTrendingFull
import com.myapplications.mywatchlist.data.mappers.toGenreForCacheItemList
import com.myapplications.mywatchlist.data.mappers.toTitleItemCacheList
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import kotlinx.coroutines.flow.Flow

@Dao
interface TrendingCacheDao {

    @Query("SELECT * FROM 'title_item_cache_trending' ORDER BY id")
    fun getCachedTrendingTitles(): Flow<List<TitleItemCacheTrendingFull>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedTrendingTitles(titlesList: List<TitleItemCacheTrending>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenresForCachedTrending(genres: List<GenreForCacheItemTrending>)

    @Transaction
    suspend fun insertTrendingTitles(titlesList: List<TitleItemFull>) {
        // Inserting Trending Titles
        val ids = insertCachedTrendingTitles(titlesList.toTitleItemCacheList())

        // Inserting Genres for Trending Titles
        titlesList.forEachIndexed { index, title ->
            insertGenresForCachedTrending(genres = title.genres.toGenreForCacheItemList(ids[index]))
        }
    }

    @Query("DELETE FROM 'title_item_cache_trending'")
    suspend fun clearCachedTrendingTitles()

}