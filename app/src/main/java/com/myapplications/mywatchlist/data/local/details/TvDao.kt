package com.myapplications.mywatchlist.data.local.details

import androidx.room.*
import com.myapplications.mywatchlist.data.entities.*
import com.myapplications.mywatchlist.data.mappers.toCastMembersForTvEntity
import com.myapplications.mywatchlist.data.mappers.toListOfGenreForTvEntity
import com.myapplications.mywatchlist.data.mappers.toTvEntity
import com.myapplications.mywatchlist.domain.entities.TV

@Dao
interface TvDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTvEntity(tvEntity: TvEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenresForTvEntity(genres: List<GenreForTvEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCastMembersForTvEntity(cast: List<CastMemberForTvEntity>)

    /** The CASCADE policy will delete associates Genres and CastMembers data */
    @Delete
    suspend fun deleteTvEntity(tvEntity: TvEntity)

    @Query("SELECT * FROM tventity WHERE id=:tvId")
    suspend fun getTv(tvId: Long): TvEntityWithGenresAndCast

    @Transaction
    suspend fun insertTv(tv: TV) {
        insertTvEntity(tvEntity = tv.toTvEntity())
        val genresToInsert = tv.genres.toListOfGenreForTvEntity(tv.id)
        insertGenresForTvEntity(genresToInsert)
        if (tv.cast != null) {
            val castToInsert = tv.cast.toCastMembersForTvEntity(tv.id)
            insertCastMembersForTvEntity(castToInsert)
        }
    }

}