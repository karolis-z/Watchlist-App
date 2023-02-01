package com.myapplications.mywatchlist.data.entities

import androidx.room.*
import com.myapplications.mywatchlist.domain.entities.*
import java.time.LocalDate

@Entity
data class TvEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,               //This represents the mediaId (could match Id of TV)
    val name: String,
    val overview: String?,      // A Title can possibly not have an overview text
    val popularity: Double?,
    val tagline: String?,       // A Title can possibly not have a tagline text
    val posterLink: String?,    // A Title can possibly not have a poster associated with it
    val backdropLink: String?,  // A Title can possibly not have a backdrop associated with it
    val status: TvStatus,
    val releaseDate: LocalDate?,// A Title can possibly not have a release date associated with it
    val lastAirDate: LocalDate?,
    val numberOfSeasons: Int,
    val numberOfEpisodes: Int,
    val spokenLanguages: List<String>?,
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

@Entity(
    primaryKeys = ["videoId", "tvId"],
    foreignKeys = [ForeignKey(
        entity = TvEntity::class,
        parentColumns = ["id"],
        childColumns = ["tvId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class YtVideoForTvEntity(
    val videoId: String,
    val link: String,
    val name: String,
    val type: YtVideoType,
    val tvId: Long
)

@Entity(
    primaryKeys = ["parentTvId", "mediaId"],
    foreignKeys = [ForeignKey(
        entity = TvEntity::class,
        parentColumns = ["id"],
        childColumns = ["parentTvId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class TitleItemRecommendedTvEntity(
    val parentTvId: Long,
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
    primaryKeys = ["parentTvId", "mediaId"],
    foreignKeys = [ForeignKey(
        entity = TvEntity::class,
        parentColumns = ["id"],
        childColumns = ["parentTvId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class TitleItemSimilarTvEntity(
    val parentTvId: Long,
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

data class TvEntityFull(
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
    val cast: List<CastMember>,

    @Relation(
        parentColumn = "id",
        entityColumn = "tvId",
        entity = YtVideoForTvEntity::class,
        projection = ["videoId","link", "name", "type"]
    )
    val videos: List<YtVideo>,

    @Relation(
        parentColumn = "id",
        entityColumn = "parentTvId",
        entity = TitleItemRecommendedTvEntity::class,
         projection = [ "name", "type", "mediaId", "overview", "posterLink", "releaseDate", "voteCount", "voteAverage", "isWatchlisted"]
    )
    val recommendations: List<TitleItemMinimal>,

    @Relation(
        parentColumn = "id",
        entityColumn = "parentTvId",
        entity = TitleItemSimilarTvEntity::class,
         projection = [ "name", "type", "mediaId", "overview", "posterLink", "releaseDate", "voteCount", "voteAverage", "isWatchlisted"]
    )
    val similar: List<TitleItemMinimal>
)
