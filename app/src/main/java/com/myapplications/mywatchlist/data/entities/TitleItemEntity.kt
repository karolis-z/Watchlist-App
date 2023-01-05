package com.myapplications.mywatchlist.data.entities

import androidx.room.*
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleType
import java.time.LocalDate

@Entity
data class TitleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val type: TitleType,
    val mediaId: Long,
    val overview: String?,  // A Title can possibly not have an overview text
    val posterLink: String?,// A Title can possibly not have a poster associated with it
    val releaseDate: LocalDate,
    val voteCount: Long,
    val voteAverage: Double,
)

@Entity(
    primaryKeys = ["id", "titleId"],
    foreignKeys = [ForeignKey(
        entity = TitleItemEntity::class,
        parentColumns = ["id"],
        childColumns = ["titleId"]
    )]
)
data class GenreForTitleEntity(
    val id: Long,
    val name: String,
    val titleId: Long
)


data class TitleItemWithGenre(
    @Embedded val titleItem: TitleItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "titleId",
        entity = GenreForTitleEntity::class,
        projection = ["id","name"]
    )
    val genres: List<Genre>,
)


