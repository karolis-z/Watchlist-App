package com.myapplications.mywatchlist.domain.entities

import java.time.LocalDate

data class Movie(
    val id: Long,               //This represents the mediaId (could match Id of TV)
    val name: String,
    val overview: String?,      // A Title can possibly not have an overview text
    val tagline: String?,       // A Title can possibly not have a tagline text
    val posterLink: String?,    // A Title can possibly not have a poster associated with it
    val backdropLink: String?,  // A Title can possibly not have a backdrop associated with it
    val genres: List<Genre>,
    val cast: List<CastMember>?,
    val videos: List<String>?,  // A Title can possibly not have videos associated with it
    val status: Status,
    val releaseDate: LocalDate?,// A Title can possibly not have a release date associated with it
    val revenue: Long?,         // A Title can possibly not have revenue associates with it
    val voteCount: Long,
    val voteAverage: Double,
    val isWatchlisted: Boolean
)

enum class Status{
    Released,
    Rumored,
    Planned,
    InProduction,
    PostProduction,
    Cancelled
}
