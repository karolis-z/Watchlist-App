package com.myapplications.mywatchlist.data.local.details

import androidx.room.*
import com.myapplications.mywatchlist.data.entities.*
import com.myapplications.mywatchlist.data.mappers.*
import com.myapplications.mywatchlist.domain.entities.TV

@Dao
interface TvDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTvEntity(tvEntity: TvEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenresForTvEntity(genres: List<GenreForTvEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCastMembersForTvEntity(cast: List<CastMemberForTvEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYtVideosForTvEntity(videos: List<YtVideoForTvEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendedForTvEntity(titleItems: List<TitleItemRecommendedTvEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimilarForTvEntity(titleItems: List<TitleItemSimilarTvEntity>)

    /** The CASCADE policy will delete associates Genres and CastMembers data */
    @Delete
    suspend fun deleteTvEntity(tvEntity: TvEntity)

    @Query("SELECT * FROM tventity WHERE id=:tvId")
    suspend fun getTv(tvId: Long): TvEntityFull

    @Transaction
    suspend fun insertTv(tv: TV) {
        insertTvEntity(tvEntity = tv.toTvEntity())

        // Inserting genres
        val genresToInsert = tv.genres.toListOfGenreForTvEntity(tv.id)
        insertGenresForTvEntity(genresToInsert)

        // Inserting cast if tv has it
        if (tv.cast != null) {
            val castToInsert = tv.cast.toCastMembersForTvEntity(tv.id)
            insertCastMembersForTvEntity(castToInsert)
        }

        // Inserting videos if tv has them
        if (tv.videos != null) {
            val videosToInsert = tv.videos.toListOfYtVideosForTvEntity(tv.id)
            insertYtVideosForTvEntity(videosToInsert)
        }

        // Inserting recommended TV if move has them
        if (tv.recommendations != null) {
            val recommendedToInsert =
                tv.recommendations.toTitleItemRecommendedTvEntityList(tv.id)
            insertRecommendedForTvEntity(recommendedToInsert)
        }

        // Inserting recommended TV if move has them
        if (tv.similar != null) {
            val similarToInsert =
                tv.similar.toTitleItemSimilarTvEntityList(tv.id)
            insertSimilarForTvEntity(similarToInsert)
        }
    }

}