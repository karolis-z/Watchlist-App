package com.myapplications.mywatchlist.domain.entities

import java.time.LocalDate

data class TV (
    override val id: Long,               //This represents the mediaId (could match Id of TV)
    override val name: String,
    override val overview: String?,      // A Title can possibly not have an overview text
    override val tagline: String?,       // A Title can possibly not have a tagline text
    override val posterLink: String?,    // A Title can possibly not have a poster associated with it
    override val backdropLink: String?,  // A Title can possibly not have a backdrop associated with it
    override val genres: List<Genre>,
    override val cast: List<CastMember>?,
    override val videos: List<YtVideo>?,  // A Title can possibly not have videos associated with it
    val status: TvStatus,
    override val releaseDate: LocalDate?,// A Title can possibly not have a release date associated with it
    val lastAirDate: LocalDate?,
    val numberOfSeasons: Int,
    val numberOfEpisodes: Int,
    override val voteCount: Long,
    override val voteAverage: Double,
    override val isWatchlisted: Boolean,
    override val recommendations: List<TitleItemMinimal>?,
    override val similar: List<TitleItemMinimal>?
) : Title

enum class TvStatus {
    Ended,
    ReturningSeries, // "Returning Series"
    Pilot,
    InProduction, // "In Production"
    Planned,
    Cancelled, // "Canceled"
    Unknown // Used for exceptions when api does not have a valid value.
}