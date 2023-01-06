package com.myapplications.mywatchlist.data.local.genres

import androidx.room.*
import com.myapplications.mywatchlist.data.entities.GenreEntity

@Dao
interface GenresDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGenres(genres: List<GenreEntity>)

    @Query("DELETE FROM `genre-entity`")
    suspend fun deleteGenres()

    @Transaction
    suspend fun deleteAllAndSaveNewGenres(genres: List<GenreEntity>) {
        deleteGenres()
        saveGenres(genres)
    }

    @Query("SELECT * FROM `genre-entity`")
    suspend fun getAllGenres(): List<GenreEntity>

}