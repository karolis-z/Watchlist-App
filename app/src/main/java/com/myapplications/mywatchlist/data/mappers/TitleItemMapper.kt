package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.datastore.ApiConfiguration
import com.myapplications.mywatchlist.data.entities.TitleItemApiModel
import com.myapplications.mywatchlist.data.entities.TitleItemEntity
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleItemFull

/**
 * Converts a [TitleItemApiModel] to [TitleItemFull]
 * @param allGenres a list of [Genre] from the database to map the genre ids received from api.
 */
fun TitleItemApiModel.toTitleItemFull(
    allGenres: List<Genre>,
    apiConfiguration: ApiConfiguration
): TitleItemFull {
    val setOfGenreIds = this.genres.map { it.toLong() }.toSet()
    return TitleItemFull(
        id = this.id,
        name = this.name,
        type = this.type.toTitleType(),
        mediaId = this.mediaId,
        overview = this.overview,
        posterLink = apiConfiguration.baseImageUrl +
                apiConfiguration.posterDefaultSize + this.posterLinkEnding,
        genres = allGenres.filter { it.id in setOfGenreIds },
        releaseDate = this.releaseDate,
        voteCount = this.voteCount,
        voteAverage = this.voteAverage,
        isWatchlisted = false // Api model does not have this information and assumes False
    )
}

/**
 * Converts a list of [TitleItemApiModel] to a list of [TitleItemFull]
 * @param allGenres a list of [Genre] from the database to map the genre ids received from api.
 */
fun List<TitleItemApiModel>.toTitleItemsFull(
    allGenres: List<Genre>,
    apiConfiguration: ApiConfiguration
): List<TitleItemFull> {
    return this.map { it.toTitleItemFull(allGenres, apiConfiguration) }
}

/**
 * Converts [TitleItemFull] to [TitleItemEntity]
 */
fun TitleItemFull.toTitleItemEntity(): TitleItemEntity {
    return TitleItemEntity(
        name = this.name,
        type = this.type,
        mediaId = this.mediaId,
        overview = this.overview,
        posterLink = this.posterLink,
        releaseDate = this.releaseDate,
        voteCount = this.voteCount,
        voteAverage = this.voteAverage,
        isWatchlisted = this.isWatchlisted
    )
}
