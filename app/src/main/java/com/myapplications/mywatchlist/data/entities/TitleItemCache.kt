package com.myapplications.mywatchlist.data.entities

import androidx.room.*
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleType
import java.time.LocalDate

@Entity(tableName = "remote_key_trending")
data class RemoteKeyTrending(
    @PrimaryKey(autoGenerate = false)
    val cachedTitleId: Long,
    val prevKey: Int?,
    val currentPage: Int,
    val nextKey: Int?,
    val createdOn: Long
)

@Entity(tableName = "title_item_cache_trending")
data class TitleItemCacheTrending(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val name: String,
    val type: TitleType,
    val mediaId: Long,
    val overview: String?,  // A Title can possibly not have an overview text
    val popularity: Double?,
    val posterLink: String?,// A Title can possibly not have a poster associated with it
    val releaseDate: LocalDate?,
    val voteCount: Long,
    val voteAverage: Double,
    val isWatchlisted: Boolean,
    val page: Int
)


@Entity(
    tableName = "genre_for_cache_item_trending",
    primaryKeys = ["id", "titleId"],
    foreignKeys = [ForeignKey(
        entity = TitleItemCacheTrending::class,
        parentColumns = ["id"],
        childColumns = ["titleId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class GenreForCacheItemTrending(
    val id: Long,
    val name: String,
    val titleId: Long
)


data class TitleItemCacheTrendingFull(
    @Embedded val titleItem: TitleItemCacheTrending,
    @Relation(
        parentColumn = "id",
        entityColumn = "titleId",
        entity = GenreForCacheItemTrending::class,
        projection = ["id","name"]
    )
    val genres: List<Genre>,
)