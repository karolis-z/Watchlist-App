package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.entities.MovieApiModel
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.Movie

/**
 * Converts [MovieApiModel] to [Movie]
 * @param allGenres list of [Genre] from the database to map the genre ids received from api.
 */
fun MovieApiModel.toMovie(allGenres: List<Genre>): Movie {
    val setOfGenreIds = this.genres.map { it.toLong() }.toSet()
    return Movie(
        id = this.id,
        name = this.name,
        imdbId = this.imdbId,
        overview = this.overview,
        tagline = this.tagline,
        posterLink = this.posterLink,
        backdropLink = this.backdropLink,
        genres = allGenres.filter { it.id in setOfGenreIds },
        cast = this.cast,
        videos = this.videos,
        status =this.status,
        releaseDate = this.releaseDate,
        revenue = this.revenue,
        runtime = this.runtime,
        voteCount = this.voteCount,
        voteAverage = this.voteAverage,
        isWatchlisted = false // Api model does not have this information and assumes False
    )
}

/**
 * Converts a list of [MovieApiModel] to a list of [Movie]
 * @param allGenres a list of [Genre] from the database to map the genre ids received from api.
 */
fun List<MovieApiModel>.toMovies(allGenres: List<Genre>): List<Movie> {
    return this.map { it.toMovie(allGenres) }
}