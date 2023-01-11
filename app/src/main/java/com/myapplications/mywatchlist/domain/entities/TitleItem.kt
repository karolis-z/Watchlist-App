package com.myapplications.mywatchlist.domain.entities

import java.time.LocalDate

data class TitleItem(
    val id: Long,
    val name: String,
    val type: TitleType,
    val mediaId: Long,
    val overview: String?,  // A Title can possibly not have an overview text
    val posterLink: String?,// A Title can possibly not have a poster associated with it
    val genres: List<Genre>,
    val releaseDate: LocalDate?,
    val voteCount: Long,
    val voteAverage: Double,
    val isWatchlisted: Boolean
)

enum class TitleType {
    MOVIE,
    TV
}

data class Genre(
    val id: Long,
    val name: String
)
