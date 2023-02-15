package com.myapplications.mywatchlist.data.entities.cached

import androidx.room.*
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleType
import java.time.LocalDate

@Entity(tableName = "remote_key_toprated_movie")
data class RemoteKeyTopRatedMovie(
    @PrimaryKey(autoGenerate = false)
    override val cachedTitleId: Long,
    override val prevKey: Int?,
    override val currentPage: Int,
    override val nextKey: Int?,
    override val createdOn: Long
) : RemoteKey

@Entity(tableName = "title_item_cache_toprated_movie")
data class TitleItemCacheTopRatedMovie(
    @PrimaryKey(autoGenerate = true)
    override val id: Long = 0,
    override val name: String,
    override val type: TitleType,
    override val mediaId: Long,
    override val overview: String?,  // A Title can possibly not have an overview text
    override val popularity: Double?,
    override val posterLink: String?,// A Title can possibly not have a poster associated with it
    override val releaseDate: LocalDate?,
    override val voteCount: Long,
    override val voteAverage: Double,
    override val isWatchlisted: Boolean,
    val page: Int
) : TitleItemCache


@Entity(
    tableName = "genre_for_cache_item_toprated_movie",
    primaryKeys = ["id", "titleId"],
    foreignKeys = [ForeignKey(
        entity = TitleItemCacheTopRatedMovie::class,
        parentColumns = ["id"],
        childColumns = ["titleId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class GenreForCacheItemTopRatedMovie(
    override val id: Long,
    override val name: String,
    override val titleId: Long
): GenreForCacheItem


data class TitleItemCacheTopRatedMovieFull(
    @Embedded override val titleItem: TitleItemCacheTopRatedMovie,
    @Relation(
        parentColumn = "id",
        entityColumn = "titleId",
        entity = GenreForCacheItemTopRatedMovie::class,
        projection = ["id","name"]
    )
    override val genres: List<Genre>,
) : TitleItemCacheFull