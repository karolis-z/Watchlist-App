package com.myapplications.mywatchlist.domain.entities

import java.time.LocalDate

// TITLE TYPE CODES
const val MOVIE_CODE: Long = 99
const val TV_CODE: Long = 11

interface TitleItem {
    val name: String
    val type: TitleType
    val mediaId: Long
    val overview: String?
    val posterLink: String?
    val releaseDate: LocalDate?
    val voteCount: Long
    val voteAverage: Double
    val isWatchlisted: Boolean
}

/**
 * Model representing a Title in a list.
 */
data class TitleItemFull(
    val id: Long,
    override val name: String,
    override val type: TitleType,
    override val mediaId: Long,
    override val overview: String?,  // A Title can possibly not have an overview text
    val popularity: Double?,
    override val posterLink: String?,// A Title can possibly not have a poster associated with it
    val genres: List<Genre>,
    override val releaseDate: LocalDate?,
    override val voteCount: Long,
    override val voteAverage: Double,
    override val isWatchlisted: Boolean
) : TitleItem

/**
 * Model representing a minimal Title in a list without nested lists.
 */
data class TitleItemMinimal(
    override val name: String,
    override val type: TitleType,
    override val mediaId: Long,
    override val overview: String?,  // A Title can possibly not have an overview text
    override val posterLink: String?,// A Title can possibly not have a poster associated with it
    override val releaseDate: LocalDate?,
    override val voteCount: Long,
    override val voteAverage: Double,
    override val isWatchlisted: Boolean
): TitleItem

enum class TitleType(val typeCode: Long) {
    MOVIE(typeCode = MOVIE_CODE),
    TV(typeCode = TV_CODE)
}

data class Genre(
    val id: Long,
    val name: String
)
