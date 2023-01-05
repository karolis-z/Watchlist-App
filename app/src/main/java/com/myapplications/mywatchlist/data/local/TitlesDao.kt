package com.myapplications.mywatchlist.data.local

import android.util.Log
import androidx.room.*
import com.myapplications.mywatchlist.data.entities.GenreForTitleEntity
import com.myapplications.mywatchlist.data.entities.TitleItemEntity
import com.myapplications.mywatchlist.data.entities.TitleItemWithGenres
import com.myapplications.mywatchlist.data.mappers.toListOfGenreForTitleEntity
import com.myapplications.mywatchlist.data.mappers.toTitleItemEntity
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.entities.TitleType

private const val TAG = "TITLES_DAO"

@Dao
interface TitlesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTitleItemEntity(titleItemEntity: TitleItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenresForTitleEntity(genres: List<GenreForTitleEntity>)

    @Query("SELECT EXISTS(SELECT * FROM TitleItemEntity WHERE mediaId=:mediaId AND type=:type)")
    suspend fun checkIfTitleItemExists(type: TitleType, mediaId: Long): Boolean

    @Query("SELECT * FROM titleitementity WHERE mediaId=:mediaId AND type=:type")
    suspend fun getTitleItemsWithGenresByTypeAndMediaId(type: TitleType, mediaId: Long): TitleItemWithGenres

    @Transaction
    suspend fun insertTitleItem(titleItem: TitleItem) {
        val titleItemId = insertTitleItemEntity(titleItemEntity = titleItem.toTitleItemEntity())
        Log.d(TAG, "bookmarkTitleItem: titleitemId = $titleItemId")
        if (titleItemId >= 0) {
            val genresToInsert = titleItem.genres.toListOfGenreForTitleEntity(titleItemId)
            Log.d(TAG, "genresToInsert = $genresToInsert")
            insertGenresForTitleEntity(genresToInsert)
        }
    }

    @Transaction
    suspend fun updateTitleItem(titleItem: TitleItem) {
        // Get the existing title item entity and its genres.
        val titleItemWithGenres = getTitleItemsWithGenresByTypeAndMediaId(
            type = titleItem.type,
            mediaId = titleItem.mediaId
        )
        Log.d(TAG, "updateTitleItem: titleItemWithGenres $titleItemWithGenres")
        TODO("Finish this")

        // Create a new one using given titleItem values

        // Update TitleItem
    }
}