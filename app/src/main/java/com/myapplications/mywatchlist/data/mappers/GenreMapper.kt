package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.entities.GenreEntity
import com.myapplications.mywatchlist.data.entities.GenreForMovieEntity
import com.myapplications.mywatchlist.data.entities.GenreForTitleEntity
import com.myapplications.mywatchlist.data.entities.GenreForTvEntity
import com.myapplications.mywatchlist.domain.entities.Genre

/**
 * Converts a [Genre] to a [GenreEntity]
 */
fun Genre.toGenreEntity(): GenreEntity {
    return GenreEntity(id = this.id, name = this.name)
}

/**
 * Convert a list of [Genre] to a list of [GenreEntity]
 */
fun List<Genre>.toGenreEntities(): List<GenreEntity> {
    return this.map {
        it.toGenreEntity()
    }
}

/**
 * Converts a [GenreEntity] to [Genre]
 */
fun GenreEntity.toGenre(): Genre {
    return Genre(id = this.id, name = this.name)
}

/**
 * Convert a list of [GenreEntity] to a list of [Genre]
 */
fun List<GenreEntity>.toGenres(): List<Genre> {
    return this.map {
        it.toGenre()
    }
}

/**
 * Convert a [Genre] to [GenreForTitleEntity] for using in local database
 * @param titleId is the id of [TitleItemEntity] with which [GenreForTitleEntity] has to be associated
 */
fun Genre.toGenreForTitleEntity(titleId: Long): GenreForTitleEntity {
    return GenreForTitleEntity(id = this.id, name = this.name, titleId = titleId)
}

/**
 * Convert a list of [Genre] to a list of [GenreForTitleEntity]
 * @param titleId is the id of [TitleItemEntity] with which [GenreForTitleEntity] has to be associated
 */
fun List<Genre>.toListOfGenreForTitleEntity(titleId: Long): List<GenreForTitleEntity> {
    return this.map {
        it.toGenreForTitleEntity(titleId)
    }
}

/**
 * Convert a [Genre] to [GenreForMovieEntity] for using in local database
 * @param movieId is the id of [MovieEntity] with which [GenreForMovieEntity] has to be associated
 */
fun Genre.toGenreForMovieEntity(movieId: Long): GenreForMovieEntity {
    return GenreForMovieEntity(id = this.id, name = this.name, movieId = movieId)
}

/**
 * Convert a list of [Genre] to a list of [GenreForMovieEntity]
 * @param movieId is the id of [MovieEntity] with which [GenreForMovieEntity] has to be associated
 */
fun List<Genre>.toListOfGenreForMovieEntity(movieId: Long): List<GenreForMovieEntity> {
    return this.map {
        it.toGenreForMovieEntity(movieId)
    }
}

/**
 * Convert a [Genre] to [GenreForTvEntity] for using in local database
 * @param tvId is the id of [TvEntity] with which [GenreForTvEntity] has to be associated
 */
fun Genre.toGenreForTvEntity(tvId: Long): GenreForTvEntity {
    return GenreForTvEntity(id = this.id, name = this.name, tvId = tvId)
}

/**
 * Convert a list of [Genre] to a list of [GenreForTvEntity]
 * @param tvId is the id of [TvEntity] with which [GenreForTvEntity] has to be associated
 */
fun List<Genre>.toListOfGenreForTvEntity(tvId: Long): List<GenreForTvEntity> {
    return this.map {
        it.toGenreForTvEntity(tvId)
    }
}