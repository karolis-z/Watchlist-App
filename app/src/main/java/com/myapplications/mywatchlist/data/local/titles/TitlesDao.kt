package com.myapplications.mywatchlist.data.local.titles

import androidx.room.*
import com.myapplications.mywatchlist.data.entities.GenreForTitleEntity
import com.myapplications.mywatchlist.data.entities.TitleItemEntity
import com.myapplications.mywatchlist.data.entities.TitleItemWithGenres
import com.myapplications.mywatchlist.data.mappers.toListOfGenreForTitleEntity
import com.myapplications.mywatchlist.data.mappers.toTitleItemEntity
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.entities.TitleType
import kotlinx.coroutines.flow.Flow

private const val TAG = "TITLES_DAO"

@Dao
interface TitlesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTitleItemEntity(titleItemEntity: TitleItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenresForTitleEntity(genres: List<GenreForTitleEntity>)

    @Query("SELECT EXISTS(SELECT * FROM TitleItemEntity WHERE mediaId=:mediaId AND type=:type)")
    suspend fun checkIfTitleItemExists(type: TitleType, mediaId: Long): Boolean

    @Query("SELECT * FROM TitleItemEntity WHERE mediaId=:mediaId AND type=:type AND isWatchlisted=1")
    suspend fun getWatchlistedTitleItemsWithGenresByTypeAndMediaId(type: TitleType, mediaId: Long): TitleItemWithGenres?

    @Query("SELECT * FROM TitleItemEntity WHERE mediaId=:mediaId AND type=:type")
    suspend fun getTitleItemEntityByTypeAndMediaId(type: TitleType, mediaId: Long): TitleItemEntity

    @Query("SELECT * FROM TitleItemEntity")
    suspend fun getAllTitleItems(): List<TitleItemWithGenres>?

    @Query("SELECT * FROM TitleItemEntity WHERE isWatchlisted=1")
    fun allWatchlistedTitleItems(): Flow<List<TitleItemWithGenres>>

    @Delete
    suspend fun deleteTitleItemEntity(titleItemEntity: TitleItemEntity)

    @Transaction
    suspend fun insertTitleItem(titleItemFull: TitleItemFull) {
        val titleItemId = insertTitleItemEntity(titleItemEntity = titleItemFull.toTitleItemEntity())
        if (titleItemId >= 0) {
            val genresToInsert = titleItemFull.genres.toListOfGenreForTitleEntity(titleId = titleItemId)
            insertGenresForTitleEntity(genres = genresToInsert)
        }
    }

    @Transaction
    suspend fun updateTitleItem(titleItemFull: TitleItemFull) {
        /* Updating is trickier that due to relation between TitleItemEntity and GenreForTitleEntity,
        * so it's simpler to just delete and insert new data. Deleting TitleItemEntity will also
        * delete GenreForTitleEntity because of onDelete = CASCADE policy set on GenreForTitleEntity. */
        val titleItemEntity = getTitleItemEntityByTypeAndMediaId(
            type = titleItemFull.type,
            mediaId = titleItemFull.mediaId
        )
        deleteTitleItemEntity(titleItemEntity)

        // Inserting new TitleItemEntity and GenreForTitleEntity
        insertTitleItem(titleItemFull)
    }

    @Transaction
    suspend fun deleteTitleItem(titleItemFull: TitleItemFull) {
        val titleItemEntity = getTitleItemEntityByTypeAndMediaId(
            type = titleItemFull.type,
            mediaId = titleItemFull.mediaId
        )
        deleteTitleItemEntity(titleItemEntity)
    }

}