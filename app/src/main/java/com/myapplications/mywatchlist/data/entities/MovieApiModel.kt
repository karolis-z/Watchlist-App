package com.myapplications.mywatchlist.data.entities

import com.myapplications.mywatchlist.domain.entities.CastMember
import com.myapplications.mywatchlist.domain.entities.Status
import java.time.LocalDate

data class MovieApiModel(
    val id: Long,               //This represents the mediaId (could match Id of TV)
    val name: String,
    val imdbId: String?,        // A Title can possibly not have an Imdb Id.
    val overview: String?,      // A Title can possibly not have an overview text
    val tagline: String?,       // A Title can possibly not have a tagline text
    val posterLink: String?,    // A Title can possibly not have a poster associated with it
    val backdropLink: String?,  // A Title can possibly not have a backdrop associated with it
    val genres: List<Int>,
    val cast: List<CastMember>?,
    val videos: List<String>?,  // A Title can possibly not have videos associated with it
    val status: Status,
    val releaseDate: LocalDate?,// A Title can possibly not have a release date associated with it
    val revenue: Long?,         // A Title can possibly not have revenue associates with it
    val voteCount: Long,
    val voteAverage: Double
)