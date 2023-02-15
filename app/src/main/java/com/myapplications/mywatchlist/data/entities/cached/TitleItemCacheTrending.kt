package com.myapplications.mywatchlist.data.entities

import androidx.room.*
import com.myapplications.mywatchlist.data.entities.cached.GenreForCacheItem
import com.myapplications.mywatchlist.data.entities.cached.RemoteKey
import com.myapplications.mywatchlist.data.entities.cached.TitleItemCache
import com.myapplications.mywatchlist.data.entities.cached.TitleItemCacheFull
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleType
import java.time.LocalDate

@Entity(tableName = "remote_key_trending")
data class RemoteKeyTrending(
    @PrimaryKey(autoGenerate = false)
    override val cachedTitleId: Long,
    override val prevKey: Int?,
    override val currentPage: Int,
    override val nextKey: Int?,
    override val createdOn: Long
) : RemoteKey

@Entity(tableName = "title_item_cache_trending")
data class TitleItemCacheTrending(
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
    override val isWatchlisted: Boolean
) : TitleItemCache


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
    override val id: Long,
    override val name: String,
    override val titleId: Long
) : GenreForCacheItem


data class TitleItemCacheTrendingFull(
    @Embedded override val titleItem: TitleItemCacheTrending,
    @Relation(
        parentColumn = "id",
        entityColumn = "titleId",
        entity = GenreForCacheItemTrending::class,
        projection = ["id","name"]
    )
    override val genres: List<Genre>,
) : TitleItemCacheFull