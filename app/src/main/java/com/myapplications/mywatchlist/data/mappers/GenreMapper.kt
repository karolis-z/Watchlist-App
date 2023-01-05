package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.entities.GenreEntity
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