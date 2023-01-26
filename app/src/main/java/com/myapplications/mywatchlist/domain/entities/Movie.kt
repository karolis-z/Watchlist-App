package com.myapplications.mywatchlist.domain.entities

import java.time.LocalDate

data class Movie(
    override val id: Long,               //This represents the mediaId (could match Id of TV)
    override val name: String,
    val imdbId: String?,        // A Title can possibly not have an Imdb Id.
    override val overview: String?,      // A Title can possibly not have an overview text
    override val tagline: String?,       // A Title can possibly not have a tagline text
    override val posterLink: String?,    // A Title can possibly not have a poster associated with it
    override val backdropLink: String?,  // A Title can possibly not have a backdrop associated with it
    override val genres: List<Genre>,
    override val cast: List<CastMember>?,
    override val videos: List<YtVideo>?,  // A Title can possibly not have videos associated with it
    val status: MovieStatus,
    override val releaseDate: LocalDate?,// A Title can possibly not have a release date associated with it
    val revenue: Long?,         // A Title can possibly not have revenue associated with it
    val runtime: Int?,          // A Title can possibly not have runtime associated with it
    override val voteCount: Long,
    override val voteAverage: Double,
    override val isWatchlisted: Boolean
) : Title

enum class MovieStatus{
    Released,
    Rumored,
    Planned,
    InProduction,
    PostProduction,
    Cancelled,
    Unknown // Used for exceptions when api does not have a valid value.
}
