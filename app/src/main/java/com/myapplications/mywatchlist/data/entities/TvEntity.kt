package com.myapplications.mywatchlist.data.entities

import androidx.room.*
import com.myapplications.mywatchlist.domain.entities.CastMember
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TvStatus
import java.time.LocalDate

@Entity
data class TvEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,               //This represents the mediaId (could match Id of TV)
    val name: String,
    val overview: String?,      // A Title can possibly not have an overview text
    val tagline: String?,       // A Title can possibly not have a tagline text
    val posterLink: String?,    // A Title can possibly not have a poster associated with it
    val backdropLink: String?,  // A Title can possibly not have a backdrop associated with it
//    val genres: List<Genre>,
//    val cast: List<CastMember>?,
    val videos: List<String>?,  // A Title can possibly not have videos associated with it
    val status: TvStatus,
    val releaseDate: LocalDate?,// A Title can possibly not have a release date associated with it
    val lastAirDate: LocalDate?,
    val numberOfSeasons: Int,
    val numberOfEpisodes: Int,
    val voteCount: Long,
    val voteAverage: Double,
    val isWatchlisted: Boolean
)

@Entity(
    primaryKeys = ["id", "tvId"],
    foreignKeys = [ForeignKey(
        entity = TvEntity::class,
        parentColumns = ["id"],
        childColumns = ["tvId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class GenreForTvEntity(
    val id: Long,
    val name: String,
    val tvId: Long
)

@Entity(
    primaryKeys = ["id", "tvId"],
    foreignKeys = [ForeignKey(
        entity = TvEntity::class,
        parentColumns = ["id"],
        childColumns = ["tvId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class CastMemberForTvEntity(
    val id: Long,
    val name: String,
    val character: String,
    val pictureLink: String?,
    val tvId: Long
)

data class TvEntityWithGenresAndCast(
    @Embedded val tv: TvEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "tvId",
        entity = GenreForTvEntity::class,
        projection = ["id","name"]
    )
    val genres: List<Genre>,

    @Relation(
        parentColumn = "id",
        entityColumn = "tvId",
        entity = CastMemberForTvEntity::class,
        projection = ["id","name", "character", "pictureLink"]
    )
    val cast: List<CastMember>
)
