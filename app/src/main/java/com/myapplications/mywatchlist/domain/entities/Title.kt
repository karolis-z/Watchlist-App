package com.myapplications.mywatchlist.domain.entities

import java.time.LocalDate

interface Title {
    val id: Long
    val name: String
    val overview: String?
    val tagline: String?
    val posterLink: String?
    val backdropLink: String?
    val genres: List<Genre>
    val cast: List<CastMember>?
    val videos: List<YtVideo>?
    val releaseDate: LocalDate?
    val voteCount: Long
    val voteAverage: Double
    val isWatchlisted: Boolean
}
