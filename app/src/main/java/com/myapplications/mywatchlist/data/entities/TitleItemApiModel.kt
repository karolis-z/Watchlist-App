package com.myapplications.mywatchlist.data.entities

import java.time.LocalDate

data class TitleItemApiModel(
    val id: Long,
    val name: String,
    val type: TitleTypeApiModel,
    val mediaId: Long,
    val overview: String?,
    val posterLink: String?,
    val genres: List<Int>,
    val releaseDate: LocalDate,
    val voteCount: Long,
    val voteAverage: Double,
)

enum class TitleTypeApiModel(val propertyName: String) {
    MOVIE(propertyName = "movie"),
    TV(propertyName = "tv"),
    UNKNOWN(propertyName = "unknown")
}

