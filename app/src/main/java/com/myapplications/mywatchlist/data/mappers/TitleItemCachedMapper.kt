package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.entities.TitleItemCacheTrending
import com.myapplications.mywatchlist.domain.entities.TitleItemFull

fun TitleItemFull.toTitleItemCacheTrending(page: Int): TitleItemCacheTrending {
    return TitleItemCacheTrending(
        id = this.id,
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