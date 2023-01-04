package com.myapplications.mywatchlist.domain.entities

import java.time.LocalDate

data class TitleItem(
    val id: Long,
    val name: String,
    val type: TitleType,
    val mediaId: Long,
    val overview: String,
    val posterLink: String,
    val genres: List<Genre>,
    val releaseDate: LocalDate,
    val voteCount: Long,
    val voteAverage: Double,
)

enum class TitleType {
    MOVIE,
    TV
}

data class Genre(
    val id: Long,
    val name: String
)
