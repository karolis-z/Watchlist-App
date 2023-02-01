package com.myapplications.mywatchlist.data.entities

import java.time.LocalDate

interface ITitleItemApiModel {
    val id: Long
    val name: String
    val type: TitleTypeApiModel
    val mediaId: Long
    val overview: String?
    val posterLinkEnding: String?
    val releaseDate: LocalDate?
    val voteCount: Long
    val voteAverage: Double
}

data class TitleItemApiModel(
    override val id: Long,
    override val name: String,
    override val type: TitleTypeApiModel,
    override val mediaId: Long,
    override val overview: String?,
    val popularity: Double?,
    override val posterLinkEnding: String?,
    val genres: List<Int>,
    override val releaseDate: LocalDate?,
    override val voteCount: Long,
    override val voteAverage: Double,
) : ITitleItemApiModel

enum class TitleTypeApiModel(val propertyName: String) {
    MOVIE(propertyName = "movie"),
    TV(propertyName = "tv"),
}

/**
 * Model representing a TitleItem but without genres or other nested objects for minimal info.
 */
data class TitleItemMinimalApiModel(
    override val id: Long,
    override val name: String,
    override val type: TitleTypeApiModel,
    override val mediaId: Long,
    override val overview: String?,
    override val posterLinkEnding: String?,
    override val releaseDate: LocalDate?,
    override val voteCount: Long,
    override val voteAverage: Double,
) : ITitleItemApiModel

