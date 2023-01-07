package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.entities.TitleItemWithGenres
import com.myapplications.mywatchlist.domain.entities.TitleItem

/**
 * Converts a [TitleItemWithGenres] to a [TitleItem]
 */
fun TitleItemWithGenres.toTitleItem(): TitleItem {
    return TitleItem(
        id = this.titleItem.id,
        name = this.titleItem.name,
        type = this.titleItem.type,
        mediaId = this.titleItem.mediaId,
        overview = this.titleItem.overview,
        posterLink = this.titleItem.posterLink,
        genres = this.genres,
        releaseDate = this.titleItem.releaseDate,
        voteCount = this.titleItem.voteCount,
        voteAverage = this.titleItem.voteAverage,
        isWatchlisted = this.titleItem.isWatchlisted
    )
}

/**
 * Converts a list of [TitleItemWithGenres] to a list of [TitleItem]
 */
fun List<TitleItemWithGenres>.toTitleItems(): List<TitleItem> {
    return this.map { it.toTitleItem() }
}