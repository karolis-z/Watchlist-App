package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.entities.TvApiModel
import com.myapplications.mywatchlist.data.entities.TvEntity
import com.myapplications.mywatchlist.data.entities.TvEntityWithGenresCastVideos
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TV
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.entities.TitleType

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

/**
 * Converts a [TV] to a [TitleItem]
 */
fun TV.toTitleItem(): TitleItem {
    return TitleItem(
        id = 0,
        name = this.name,
        type = TitleType.TV,
        mediaId = this.id,
        overview = this.overview,
        posterLink = this.posterLink,
        genres = this.genres,
        releaseDate = this.releaseDate,
        voteCount = this.voteCount,
        voteAverage = this.voteAverage,
        isWatchlisted = this.isWatchlisted
    )
}

/**
 * Converts a [TV] to a [TvEntity]
 */
fun TV.toTvEntity(): TvEntity {
    return TvEntity(
        id = this.id,
        name = this.name,
        overview = this.overview,
        tagline = this.tagline,
        posterLink = this.posterLink,
        backdropLink = this.backdropLink,
        status =this.status,
        releaseDate = this.releaseDate,
        lastAirDate = this.lastAirDate,
        numberOfSeasons = this.numberOfSeasons,
        numberOfEpisodes = this.numberOfEpisodes,
        voteCount = this.voteCount,
        voteAverage = this.voteAverage,
        isWatchlisted = this.isWatchlisted
    )
}

/**
 * Converts [TvEntityWithGenresCastVideos] to [TV]
 */
fun TvEntityWithGenresCastVideos.toTv(): TV {
    return TV(
        id = this.tv.id,
        name = this.tv.name,
        overview = this.tv.overview,
        tagline = this.tv.tagline,
        posterLink = this.tv.posterLink,
        backdropLink = this.tv.backdropLink,
        genres = this.genres,
        cast = this.cast,
        videos = this.videos,
        status = this.tv.status,
        releaseDate = this.tv.releaseDate,
        lastAirDate = this.tv.lastAirDate,
        numberOfSeasons = this.tv.numberOfSeasons,
        numberOfEpisodes = this.tv.numberOfEpisodes,
        voteCount = this.tv.voteCount,
        voteAverage = this.tv.voteAverage,
        isWatchlisted = this.tv.isWatchlisted
    )
}