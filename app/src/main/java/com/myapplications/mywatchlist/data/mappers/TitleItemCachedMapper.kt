package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.entities.TitleItemCacheTrending
import com.myapplications.mywatchlist.data.entities.cached.*
import com.myapplications.mywatchlist.domain.entities.TitleItemFull

fun TitleItemCacheFull.toTitleItemFull(): TitleItemFull {
    return TitleItemFull(
        id = this.titleItem.id,
        name = this.titleItem.name,
        type = this.titleItem.type,
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

/* TODO: should consider another approach? This can potentially lead to undetected errors if another
*   implementation of [TitleItemCache] is created and it's forgotten to be included here. */
/**
 * Generic method to convert a [TitleItemFull] to a [T] of type [TitleItemCache]
 * @throws IllegalArgumentException if there is no mapping provided for subtype [T]
 * @param page determines which page the cached item belongs to. Default value 1 for subtypes of
 * [TitleItemCache] that are not being paginated
 */
inline fun <reified T : TitleItemCache> TitleItemFull.toTitleItemCache(page: Int = 1): T {

    return when (T::class) {
        TitleItemCacheDiscoverMovie::class -> TitleItemCacheDiscoverMovie(
            name = this.name,
            type = this.type,
            mediaId = this.mediaId,
            overview = this.overview,
            popularity = this.popularity,
            posterLink = this.posterLink,
            releaseDate = this.releaseDate,
            voteCount = this.voteCount,
            voteAverage = this.voteAverage,
            isWatchlisted = this.isWatchlisted,
            page = page
        ) as T
        TitleItemCacheDiscoverTV::class -> TitleItemCacheDiscoverTV(
            name = this.name,
            type = this.type,
            mediaId = this.mediaId,
            overview = this.overview,
            popularity = this.popularity,
            posterLink = this.posterLink,
            releaseDate = this.releaseDate,
            voteCount = this.voteCount,
            voteAverage = this.voteAverage,
            isWatchlisted = this.isWatchlisted,
            page = page
        ) as T
        TitleItemCachePopularMovie::class -> TitleItemCachePopularMovie(
            name = this.name,
            type = this.type,
            mediaId = this.mediaId,
            overview = this.overview,
            popularity = this.popularity,
            posterLink = this.posterLink,
            releaseDate = this.releaseDate,
            voteCount = this.voteCount,
            voteAverage = this.voteAverage,
            isWatchlisted = this.isWatchlisted,
            page = page
        ) as T
        TitleItemCachePopularTV::class -> TitleItemCachePopularTV(
            name = this.name,
            type = this.type,
            mediaId = this.mediaId,
            overview = this.overview,
            popularity = this.popularity,
            posterLink = this.posterLink,
            releaseDate = this.releaseDate,
            voteCount = this.voteCount,
            voteAverage = this.voteAverage,
            isWatchlisted = this.isWatchlisted,
            page = page
        ) as T
        TitleItemCacheSearchAll::class -> TitleItemCacheSearchAll(
            name = this.name,
            type = this.type,
            mediaId = this.mediaId,
            overview = this.overview,
            popularity = this.popularity,
            posterLink = this.posterLink,
            releaseDate = this.releaseDate,
            voteCount = this.voteCount,
            voteAverage = this.voteAverage,
            isWatchlisted = this.isWatchlisted,
            page = page
        ) as T
        TitleItemCacheSearchMovie::class -> TitleItemCacheSearchMovie(
            name = this.name,
            type = this.type,
            mediaId = this.mediaId,
            overview = this.overview,
            popularity = this.popularity,
            posterLink = this.posterLink,
            releaseDate = this.releaseDate,
            voteCount = this.voteCount,
            voteAverage = this.voteAverage,
            isWatchlisted = this.isWatchlisted,
            page = page
        ) as T
        TitleItemCacheSearchTV::class -> TitleItemCacheSearchTV(
            name = this.name,
            type = this.type,
            mediaId = this.mediaId,
            overview = this.overview,
            popularity = this.popularity,
            posterLink = this.posterLink,
            releaseDate = this.releaseDate,
            voteCount = this.voteCount,
            voteAverage = this.voteAverage,
            isWatchlisted = this.isWatchlisted,
            page = page
        ) as T
        TitleItemCacheTopRatedMovie::class -> TitleItemCacheTopRatedMovie(
            name = this.name,
            type = this.type,
            mediaId = this.mediaId,
            overview = this.overview,
            popularity = this.popularity,
            posterLink = this.posterLink,
            releaseDate = this.releaseDate,
            voteCount = this.voteCount,
            voteAverage = this.voteAverage,
            isWatchlisted = this.isWatchlisted,
            page = page
        ) as T
        TitleItemCacheTopRatedTV::class -> TitleItemCacheTopRatedTV(
            name = this.name,
            type = this.type,
            mediaId = this.mediaId,
            overview = this.overview,
            popularity = this.popularity,
            posterLink = this.posterLink,
            releaseDate = this.releaseDate,
            voteCount = this.voteCount,
            voteAverage = this.voteAverage,
            isWatchlisted = this.isWatchlisted,
            page = page
        ) as T
        TitleItemCacheTrending::class -> TitleItemCacheTrending(
            name = this.name,
            type = this.type,
            mediaId = this.mediaId,
            overview = this.overview,
            popularity = this.popularity,
            posterLink = this.posterLink,
            releaseDate = this.releaseDate,
            voteCount = this.voteCount,
            voteAverage = this.voteAverage,
            isWatchlisted = this.isWatchlisted,
        ) as T
        TitleItemCacheUpcomingMovie::class -> TitleItemCacheUpcomingMovie(
            name = this.name,
            type = this.type,
            mediaId = this.mediaId,
            overview = this.overview,
            popularity = this.popularity,
            posterLink = this.posterLink,
            releaseDate = this.releaseDate,
            voteCount = this.voteCount,
            voteAverage = this.voteAverage,
            isWatchlisted = this.isWatchlisted,
            page = page
        ) as T
        else ->
            throw IllegalArgumentException("Unsupported mapping of TitleItemCache implementation")
    }
}


/**
 * Generic method to convert a list of [TitleItemFull] to a list of [T]s of type [TitleItemCache]
 * @throws IllegalArgumentException if there is no mapping provided for subtype [T]
 * @param page determines which page the cached item belongs to. Default value 1 for subtypes of
 * [TitleItemCache] that are not being paginated
 */
inline fun <reified T : TitleItemCache> List<TitleItemFull>.toTitleItemCacheList(page: Int = 1): List<T> {
    return this.map { it.toTitleItemCache(page = page) }
}
