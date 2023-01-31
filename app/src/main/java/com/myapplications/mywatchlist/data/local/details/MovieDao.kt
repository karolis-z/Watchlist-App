package com.myapplications.mywatchlist.data.local.details

import androidx.room.*
import com.myapplications.mywatchlist.data.entities.*
import com.myapplications.mywatchlist.data.mappers.*
import com.myapplications.mywatchlist.domain.entities.Movie

@Dao
interface MovieDao {

    @Upsert
    suspend fun upsertMovie(movieEntity: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovieEntity(movieEntity: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenresForMovieEntity(genres: List<GenreForMovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCastMembersForMovieEntity(cast: List<CastMemberForMovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYtVideosForMovieEntity(videos: List<YtVideoForMovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendedForMovieEntity(titleItems: List<TitleItemRecommendedMovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimilarForMovieEntity(titleItems: List<TitleItemSimilarMovieEntity>)

    /** The CASCADE policy will delete associates Genres and CastMembers data */
    @Delete
    suspend fun deleteMovieEntity(movieEntity: MovieEntity)

    @Query("SELECT * FROM movieentity WHERE id=:movieId")
    suspend fun getMovie(movieId: Long): MovieEntityFull

    @Transaction
    suspend fun insertMovie(movie: Movie) {
        insertMovieEntity(movieEntity = movie.toMovieEntity())

        // Inserting genres
        val genresToInsert = movie.genres.toListOfGenreForMovieEntity(movie.id)
        insertGenresForMovieEntity(genresToInsert)

        // Inserting cast if movie has it
        if (movie.cast != null) {
            val castToInsert = movie.cast.toCastMembersForMovieEntity(movie.id)
            insertCastMembersForMovieEntity(castToInsert)
        }

        // Inserting videos if movie has them
        if (movie.videos != null) {
            val videosToInsert = movie.videos.toListOfYtVideosForMovieEntity(movie.id)
            insertYtVideosForMovieEntity(videosToInsert)
        }

        // Inserting recommended movies if move has them
        if (movie.recommendations != null) {
            val recommendedToInsert =
                movie.recommendations.toTitleItemRecommendedMovieEntityList(movie.id)
            insertRecommendedForMovieEntity(recommendedToInsert)
        }

        // Inserting recommended movies if move has them
        if (movie.similar != null) {
            val similarToInsert =
                movie.similar.toTitleItemSimilarMovieEntityList(movie.id)
            insertSimilarForMovieEntity(similarToInsert)
        }
    }
}