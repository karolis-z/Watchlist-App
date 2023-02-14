package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.entities.TitleItemCacheTrending
import com.myapplications.mywatchlist.data.entities.TitleItemCacheTrendingFull
import com.myapplications.mywatchlist.domain.entities.TitleItemFull

fun TitleItemFull.toTitleItemCacheTrending(page: Int): TitleItemCacheTrending {
    return TitleItemCacheTrending(
        name = this.name,
        type =this.type,
        mediaId = this.mediaId,
        overview = this.overview,
        popularity = this.popularity,
        posterLink = this.posterLink,
        releaseDate = this.releaseDate,
        voteCount = this.voteCount,
        voteAverage = this.voteAverage,
        isWatchlisted = this.isWatchlisted,
        page = page
    )
}

fun List<TitleItemFull>.toTitleItemCacheTrendingList(page: Int): List<TitleItemCacheTrending> {
    return this.map {
        it.toTitleItemCacheTrending(page = page)
    }
}

fun TitleItemCacheTrendingFull.toTitleItemFull(): TitleItemFull {
    return TitleItemFull(
        id = this.titleItem.id,
        name = this.titleItem.name,
        type =this.titleItem.type,
        mediaId = this.titleItem.mediaId,
        overview = this.titleItem.overview,
        popularity = this.titleItem.popularity,
        posterLink = this.titleItem.posterLink,
        genres = this.genres,
        releaseDate = this.titleItem.releaseDate,
        voteCount = this.titleItem.voteCount,
        voteAverage = this.titleItem.voteAverage,
        isWatchlisted = this.titleItem.isWatchlisted
    )
}

fun List<TitleItemCacheTrendingFull>.toTitleItemFullList(): List<TitleItemFull> {
    return this.map { it.toTitleItemFull() }
}