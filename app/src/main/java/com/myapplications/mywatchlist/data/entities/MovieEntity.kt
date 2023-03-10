package com.myapplications.mywatchlist.data.entities

import androidx.room.*
import com.myapplications.mywatchlist.domain.entities.*
import java.time.LocalDate

@Entity
data class MovieEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,               //This represents the mediaId (could match Id of TV)
    val name: String,
    val imdbId: String?,        // A Title can possibly not have an Imdb Id.
    val overview: String?,      // A Title can possibly not have an overview text
    val popularity: Double?,
    val tagline: String?,       // A Title can possibly not have a tagline text
    val posterLink: String?,    // A Title can possibly not have a poster associated with it
    val backdropLink: String?,  // A Title can possibly not have a backdrop associated with it
    val status: MovieStatus,
    val releaseDate: LocalDate?,// A Title can possibly not have a release date associated with it
    val revenue: Long?,         // A Title can possibly not have revenue associated with it
    val budget: Long?,
    val spokenLanguages: List<String>?,
    val runtime: Int?,          // A Title can possibly not have runtime associated with it
    val voteCount: Long,
    val voteAverage: Double,
    val isWatchlisted: Boolean
)

@Entity(
    primaryKeys = ["id", "movieId"],
    foreignKeys = [ForeignKey(
        entity = MovieEntity::class,
        parentColumns = ["id"],
        childColumns = ["movieId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class GenreForMovieEntity(
    val id: Long,
    val name: String,
    val movieId: Long
)

@Entity(
    primaryKeys = ["id", "movieId"],
    foreignKeys = [ForeignKey(
        entity = MovieEntity::class,
        parentColumns = ["id"],
        childColumns = ["movieId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class CastMemberForMovieEntity(
    val id: Long,
    val name: String,
    val character: String,
    val pictureLink: String?,
    val movieId: Long
)

@Entity(
    primaryKeys = ["videoId", "movieId"],
    foreignKeys = [ForeignKey(
        entity = MovieEntity::class,
        parentColumns = ["id"],
        childColumns = ["movieId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class YtVideoForMovieEntity(
    val videoId: String,
    val link: String,
    val name: String,
    val type: YtVideoType,
    val movieId: Long
)

@Entity(
    primaryKeys = ["parentMovieId", "mediaId"],
    foreignKeys = [ForeignKey(
        entity = MovieEntity::class,
        parentColumns = ["id"],
        childColumns = ["parentMovieId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class TitleItemRecommendedMovieEntity(
    val parentMovieId: Long,
    val name: String,
    val type: TitleType,
    val mediaId: Long,
    val overview: String?,
    val posterLink: String?,
    val releaseDate: LocalDate?,
    val voteCount: Long,
    val voteAverage: Double,
    val isWatchlisted: Boolean
)

@Entity(
    primaryKeys = ["parentMovieId", "mediaId"],
    foreignKeys = [ForeignKey(
        entity = MovieEntity::class,
        parentColumns = ["id"],
        childColumns = ["parentMovieId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class TitleItemSimilarMovieEntity(
    val parentMovieId: Long,
    val name: String,
    val type: TitleType,
    val mediaId: Long,
    val overview: String?,
    val posterLink: String?,
    val releaseDate: LocalDate?,
    val voteCount: Long,
    val voteAverage: Double,
    val isWatchlisted: Boolean
)

data class MovieEntityFull(
    @Embedded val movie: MovieEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "movieId",
        entity = GenreForMovieEntity::class,
        projection = ["id","name"]
    )
    val genres: List<Genre>,

    @Relation(
        parentColumn = "id",
        entityColumn = "movieId",
        entity = CastMemberForMovieEntity::class,
        projection = ["id","name", "character", "pictureLink"]
    )
    val cast: List<CastMember>,

    @Relation(
        parentColumn = "id",
        entityColumn = "movieId",
        entity = YtVideoForMovieEntity::class,
        projection = ["videoId","link", "name", "type"]
    )
    val videos: List<YtVideo>,

    @Relation(
        parentColumn = "id",
        entityColumn = "parentMovieId",
        entity = TitleItemRecommendedMovieEntity::class,
        projection = ["name", "type", "mediaId", "overview", "posterLink", "releaseDate", "voteCount", "voteAverage", "isWatchlisted"]
    )
    val recommendations: List<TitleItemMinimal>,

    @Relation(
        parentColumn = "id",
        entityColumn = "parentMovieId",
        entity = TitleItemSimilarMovieEntity::class,
         projection = ["name", "type", "mediaId", "overview", "posterLink", "releaseDate", "voteCount", "voteAverage", "isWatchlisted"]
    )
    val similar: List<TitleItemMinimal>

)
