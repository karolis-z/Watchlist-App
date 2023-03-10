package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.datastore.ApiConfiguration
import com.myapplications.mywatchlist.data.entities.TvApiModel
import com.myapplications.mywatchlist.data.entities.TvEntity
import com.myapplications.mywatchlist.data.entities.TvEntityFull
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TV
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.entities.TitleType

/**
 * Converts [TvApiModel] to [TV]
 * @param allGenres list of [Genre] from the database to map the genre ids received from api.
 */
fun TvApiModel.toTv(allGenres: List<Genre>, apiConfiguration: ApiConfiguration): TV {
    val setOfGenreIds = this.genres.map { it.toLong() }.toSet()
    return TV(
        id = this.id,
        name = this.name,
        overview = this.overview,
        popularity = this.popularity,
        tagline = this.tagline,
        posterLink = apiConfiguration.baseImageUrl
                + apiConfiguration.posterDefaultSize + this.posterLinkEnding,
        backdropLink = apiConfiguration.baseImageUrl
                + apiConfiguration.backdropDefaultSize + this.backdropLinkEnding,
        genres = allGenres.filter { it.id in setOfGenreIds },
        cast = this.cast?.withFullProfilePictureLinks(
            imagesBaseUrl = apiConfiguration.baseImageUrl,
            profileImageSize = apiConfiguration.profileDefaultSize
        ),
        videos = this.videos,
        status = this.status,
        releaseDate = this.releaseDate,
        lastAirDate = this.lastAirDate,
        numberOfSeasons = this.numberOfSeasons,
        numberOfEpisodes = this.numberOfEpisodes,
        spokenLanguages = this.spokenLanguages,
        voteCount = this.voteCount,
        voteAverage = this.voteAverage,
        isWatchlisted = false, // Api model does not have this information and assumes False
        recommendations = this.recommendations?.toTitleItemsMinimal(apiConfiguration),
        similar = this.similar?.toTitleItemsMinimal(apiConfiguration)
    )
}

/**
 * Converts a list of [TvApiModel] to a list of [TV]
 * @param allGenres a list of [Genre] from the database to map the genre ids received from api.
 */
fun List<TvApiModel>.toTvList(
    allGenres: List<Genre>,
    apiConfiguration: ApiConfiguration
): List<TV> {
    return this.map { it.toTv(allGenres, apiConfiguration) }
}

/**
 * Converts a [TV] to a [TitleItemFull]
 */
fun TV.toTitleItemFull(): TitleItemFull {
    return TitleItemFull(
        id = ("${TitleType.TV.typeCode}${this.id}").toLong(),
        name = this.name,
        type = TitleType.TV,
        mediaId = this.id,
        overview = this.overview,
        popularity = this.popularity,
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
        popularity = this.popularity,
        tagline = this.tagline,
        posterLink = this.posterLink,
        backdropLink = this.backdropLink,
        status = this.status,
        releaseDate = this.releaseDate,
        lastAirDate = this.lastAirDate,
        numberOfSeasons = this.numberOfSeasons,
        numberOfEpisodes = this.numberOfEpisodes,
        spokenLanguages = this.spokenLanguages,
        voteCount = this.voteCount,
        voteAverage = this.voteAverage,
        isWatchlisted = this.isWatchlisted
    )
}

/**
 * Converts [TvEntityFull] to [TV]
 */
fun TvEntityFull.toTv(): TV {
    return TV(
        id = this.tv.id,
        name = this.tv.name,
        overview = this.tv.overview,
        popularity = this.tv.popularity,
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
        spokenLanguages = this.tv.spokenLanguages,
        voteCount = this.tv.voteCount,
        voteAverage = this.tv.voteAverage,
        isWatchlisted = this.tv.isWatchlisted,
        recommendations = this.recommendations,
        similar = this.similar
    )
}