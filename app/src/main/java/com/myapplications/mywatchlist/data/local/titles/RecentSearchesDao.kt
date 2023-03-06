package com.myapplications.mywatchlist.data.local.titles

import androidx.room.*
import com.myapplications.mywatchlist.data.entities.RecentSearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(recentSearchEntity: RecentSearchEntity)

    @Query("SELECT * FROM 'recent_searches' ORDER BY searchedDateTime DESC")
    fun getRecentSearches(): Flow<List<RecentSearchEntity>>

    @Query("SELECT COUNT (id) FROM 'recent_searches'")
    suspend fun getCountOfEntries(): Long

    @Query("DELETE FROM 'recent_searches' " +
            "WHERE searchedDateTime = (SELECT MIN(searchedDateTime) FROM 'recent_searches')"
    )
    suspend fun deleteOldestRecentSearch()

}