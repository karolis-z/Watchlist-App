package com.myapplications.mywatchlist.data.local.details

import androidx.room.*
import com.myapplications.mywatchlist.data.entities.*
import com.myapplications.mywatchlist.data.mappers.toCastMembersForMovieEntity
import com.myapplications.mywatchlist.data.mappers.toListOfGenreForMovieEntity
import com.myapplications.mywatchlist.data.mappers.toListOfYtVideosForMovieEntity
import com.myapplications.mywatchlist.data.mappers.toMovieEntity
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

    /** The CASCADE policy will delete associates Genres and CastMembers data */
    @Delete
    suspend fun deleteMovieEntity(movieEntity: MovieEntity)

    @Query("SELECT * FROM movieentity WHERE id=:movieId")
    suspend fun getMovie(movieId: Long): MovieEntityWithGenresCastVideos

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
    }
}