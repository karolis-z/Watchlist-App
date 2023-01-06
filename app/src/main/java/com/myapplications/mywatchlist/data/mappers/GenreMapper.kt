package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.entities.GenreEntity
import com.myapplications.mywatchlist.data.entities.GenreForTitleEntity
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