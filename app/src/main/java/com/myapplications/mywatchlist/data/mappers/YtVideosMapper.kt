package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.entities.YtVideoForMovieEntity
import com.myapplications.mywatchlist.data.entities.YtVideoForTvEntity
import com.myapplications.mywatchlist.domain.entities.YtVideo

/**
 * Converts a [YtVideo] to [YtVideoForMovieEntity].
 * @param movieId is the id of the Movie the Videos are associated with.
 */
fun YtVideo.toYtVideoForMovieEntity(movieId: Long): YtVideoForMovieEntity {
    return YtVideoForMovieEntity(
        videoId = this.videoId,
        link = this.link,
        name = this.name,
        type = this.type,
        movieId = movieId
    )
}

/**
 * Converts a [YtVideo] to [YtVideoForTvEntity].
 * @param tvId is the id of the TV the Videos are associated with.
 */
fun YtVideo.toYtVideoForTvEntity(tvId: Long): YtVideoForTvEntity {
    return YtVideoForTvEntity(
        videoId = this.videoId,
        link = this.link,
        name = this.name,
        type = this.type,
        tvId = tvId
    )
}

/**
 * Converts a list of [YtVideo]s to a list of [YtVideoForMovieEntity].
 * @param movieId is the id of the Movie the Videos are associated with.
 */
fun List<YtVideo>.toListOfYtVideosForMovieEntity(movieId: Long): List<YtVideoForMovieEntity> {
    return this.map {
        it.toYtVideoForMovieEntity(movieId = movieId)
    }
}

/**
 * Converts a list of [YtVideo]s to a list of [YtVideoForTvEntity].
 * @param tvId is the id of the TV the Videos are associated with.
 */
fun List<YtVideo>.toListOfYtVideosForTvEntity(tvId: Long): List<YtVideoForTvEntity> {
    return this.map {
        it.toYtVideoForTvEntity(tvId = tvId)
    }
}