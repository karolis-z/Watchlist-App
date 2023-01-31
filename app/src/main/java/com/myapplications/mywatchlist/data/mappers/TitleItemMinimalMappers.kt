package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.datastore.ApiConfiguration
import com.myapplications.mywatchlist.data.entities.*
import com.myapplications.mywatchlist.domain.entities.TitleItemMinimal


/**
 * Converts a [TitleItemMinimalApiModel] to [TitleItemMinimal]
 * @param apiConfiguration the current [ApiConfiguration] to get base image urls and image sizes
 * needed to construct a full url.
 */
fun TitleItemMinimalApiModel.toTitleItemMinimal(
    apiConfiguration: ApiConfiguration
): TitleItemMinimal {
    return TitleItemMinimal(
//        id = this.id,
        name = this.name,
        type = this.type.toTitleType(),
        mediaId = this.mediaId,
        overview = this.overview,
        posterLink = apiConfiguration.baseImageUrl +
                apiConfiguration.posterDefaultSize + this.posterLinkEnding,
        releaseDate = this.releaseDate,
        voteCount = this.voteCount,
        voteAverage = this.voteAverage,
        isWatchlisted = false // Api model does not have this information and assumes False
    )
}

/**
 * Converts a list of [TitleItemMinimalApiModel] to a list of [TitleItemMinimal]
 * @param apiConfiguration the current [ApiConfiguration] to get base image urls and image sizes
 * needed to construct a full url.
 */
fun List<TitleItemMinimalApiModel>.toTitleItemsMinimal(
    apiConfiguration: ApiConfiguration
): List<TitleItemMinimal> {
    return this.map { it.toTitleItemMinimal(apiConfiguration) }
}

/**
 * Converts a [TitleItemMinimal] to a [TitleItemRecommendedMovieEntity]
 * @param parentMediaId is the media id of the parent Movie that holds this recommended title.
 */
fun TitleItemMinimal.toTitleItemRecommendedMovieEntity(
    parentMediaId: Long
): TitleItemRecommendedMovieEntity {
    return TitleItemRecommendedMovieEntity(
        parentMovieId = parentMediaId,
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

/**
 * Converts a list of [TitleItemMinimal] to a list of [TitleItemRecommendedMovieEntity]
 * @param parentMediaId is the media id of the parent Movie that holds this recommended titles list.
 */
fun List<TitleItemMinimal>.toTitleItemRecommendedMovieEntityList(
    parentMediaId: Long
): List<TitleItemRecommendedMovieEntity> {
    return this.map {
        it.toTitleItemRecommendedMovieEntity(parentMediaId = parentMediaId)
    }
}

/**
 * Converts a [TitleItemMinimal] to a [TitleItemRecommendedTvEntity]
 * @param parentMediaId is the media id of the parent TV that holds this recommended title.
 */
fun TitleItemMinimal.toTitleItemRecommendedTvEntity(
    parentMediaId: Long
): TitleItemRecommendedTvEntity {
    return TitleItemRecommendedTvEntity(
        parentTvId = parentMediaId,
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

/**
 * Converts a list of [TitleItemMinimal] to a list of [TitleItemRecommendedTvEntity]
 * @param parentMediaId is the media id of the parent TV that holds this recommended titles list.
 */
fun List<TitleItemMinimal>.toTitleItemRecommendedTvEntityList(
    parentMediaId: Long
): List<TitleItemRecommendedTvEntity> {
    return this.map {
        it.toTitleItemRecommendedTvEntity(parentMediaId = parentMediaId)
    }
}

/**
 * Converts a [TitleItemMinimal] to a [TitleItemSimilarMovieEntity]
 * @param parentMediaId is the media id of the parent Movie that holds this similar title.
 */
fun TitleItemMinimal.toTitleItemSimilarMovieEntity(parentMediaId: Long):
        TitleItemSimilarMovieEntity {
    return TitleItemSimilarMovieEntity(
        parentMovieId = parentMediaId,
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

/**
 * Converts a list of [TitleItemMinimal] to a list of [TitleItemSimilarMovieEntity]
 * @param parentMediaId is the media id of the parent Movie that holds this similar titles list.
 */
fun List<TitleItemMinimal>.toTitleItemSimilarMovieEntityList(
    parentMediaId: Long
): List<TitleItemSimilarMovieEntity> {
    return this.map {
        it.toTitleItemSimilarMovieEntity(parentMediaId = parentMediaId)
    }
}

/**
 * Converts a [TitleItemMinimal] to a [TitleItemSimilarTvEntity]
 * @param parentMediaId is the media id of the parent TV that holds this similar title.
 */
fun TitleItemMinimal.toTitleItemSimilarTvEntity(parentMediaId: Long):
        TitleItemSimilarTvEntity {
    return TitleItemSimilarTvEntity(
        parentTvId = parentMediaId,
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

/**
 * Converts a list of [TitleItemMinimal] to a list of [TitleItemSimilarTvEntity]
 * @param parentMediaId is the media id of the parent TV that holds this similar titles list.
 */
fun List<TitleItemMinimal>.toTitleItemSimilarTvEntityList(
    parentMediaId: Long
): List<TitleItemSimilarTvEntity> {
    return this.map {
        it.toTitleItemSimilarTvEntity(parentMediaId = parentMediaId)
    }
}


