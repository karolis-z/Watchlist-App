package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.entities.TvApiModel
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TV

/**
 * Converts [TvApiModel] to [TV]
 * @param allGenres list of [Genre] from the database to map the genre ids received from api.
 */
fun TvApiModel.toTv(allGenres: List<Genre>): TV {
    val setOfGenreIds = this.genres.map { it.toLong() }.toSet()
    return TV(
        id = this.id,
        name = this.name,
        overview = this.overview,
        tagline = this.tagline,
        posterLink = this.posterLink,
        backdropLink = this.backdropLink,
        genres = allGenres.filter { it.id in setOfGenreIds },
        cast = this.cast,
        videos = this.videos,
        status =this.status,
        releaseDate = this.releaseDate,
        lastAirDate = this.lastAirDate,
        numberOfSeasons = this.numberOfSeasons,
        numberOfEpisodes = this.numberOfEpisodes,
        voteCount = this.voteCount,
        voteAverage = this.voteAverage,
        isWatchlisted = false // Api model does not have this information and assumes False
    )
}

/**
 * Converts a list of [TvApiModel] to a list of [TV]
 * @param allGenres a list of [Genre] from the database to map the genre ids received from api.
 */
fun List<TvApiModel>.toTvList(allGenres: List<Genre>): List<TV> {
    return this.map { it.toTv(allGenres) }
}