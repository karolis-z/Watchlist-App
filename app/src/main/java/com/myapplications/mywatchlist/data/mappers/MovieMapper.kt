package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.datastore.ApiConfiguration
import com.myapplications.mywatchlist.data.entities.MovieApiModel
import com.myapplications.mywatchlist.data.entities.MovieEntity
import com.myapplications.mywatchlist.data.entities.MovieEntityFull
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.Movie
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.entities.TitleType

/**
 * Converts [MovieApiModel] to [Movie]
 * @param allGenres list of [Genre] from the database to map the genre ids received from api.
 */
fun MovieApiModel.toMovie(allGenres: List<Genre>, apiConfiguration: ApiConfiguration): Movie {
    val setOfGenreIds = this.genres.map { it.toLong() }.toSet()
    return Movie(
        id = this.id,
        name = this.name,
        imdbId = this.imdbId,
        overview = this.overview,
        popularity = this.popularity,
        tagline = this.tagline,
        posterLink = apiConfiguration.baseImageUrl +
                apiConfiguration.posterDefaultSize + this.posterLinkEnding,
        backdropLink = apiConfiguration.baseImageUrl +
                apiConfiguration.backdropDefaultSize + this.backdropLinkEnding,
        genres = allGenres.filter { it.id in setOfGenreIds },
        cast = this.cast?.withFullProfilePictureLinks(
            imagesBaseUrl = apiConfiguration.baseImageUrl,
            profileImageSize = apiConfiguration.profileDefaultSize
        ),
        videos = this.videos,
        status = this.status,
        releaseDate = this.releaseDate,
        revenue = this.revenue,
        budget = this.budget,
        spokenLanguages = this.spokenLanguages,
        runtime = this.runtime,
        voteCount = this.voteCount,
        voteAverage = this.voteAverage,
        isWatchlisted = false, // Api model does not have this information and assumes False
        recommendations = this.recommendations?.toTitleItemsMinimal(apiConfiguration),
        similar = this.similar?.toTitleItemsMinimal(apiConfiguration)
    )
}

/**
 * Converts a list of [MovieApiModel] to a list of [Movie]
 * @param allGenres a list of [Genre] from the database to map the genre ids received from api.
 */
fun List<MovieApiModel>.toMovies(
    allGenres: List<Genre>,
    apiConfiguration: ApiConfiguration
): List<Movie> {
    return this.map { it.toMovie(allGenres, apiConfiguration) }
}

/**
 * Converts a [Movie] to a [TitleItemFull]
 */
fun Movie.toTitleItemFull(): TitleItemFull {
    return TitleItemFull(
        id = ("${TitleType.MOVIE.typeCode}${this.id}").toLong(),
        name = this.name,
        type = TitleType.MOVIE,
        mediaId = this.id,
        overview = this.overview,
        popularity = this.popularity,
        posterLink = this.posterLink,
        genres = this.genres,
        releaseDate = this.releaseDate,
        voteCount = this.voteCount,
        voteAverage = this.voteAverage,
        isWatchlisted = this.isWatchlisted
    )
}

/**
 * Converts a [Movie] to a [MovieEntity]
 */
fun Movie.toMovieEntity(): MovieEntity {
    return MovieEntity(
        id = this.id,
        name = this.name,
        imdbId = this.imdbId,
        overview = this.overview,
        popularity = this.popularity,
        tagline = this.tagline,
        posterLink = this.posterLink,
        backdropLink = this.backdropLink,
        status = this.status,
        releaseDate = this.releaseDate,
        revenue = this.revenue,
        budget = this.budget,
        spokenLanguages = this.spokenLanguages,
        runtime = this.runtime,
        voteCount = this.voteCount,
        voteAverage = this.voteAverage,
        isWatchlisted = this.isWatchlisted
    )
}

/**
 * Converts [MovieEntityFull] to [Movie]
 */
fun MovieEntityFull.toMovie(): Movie {
    return Movie(
        id = this.movie.id,
        name = this.movie.name,
        imdbId = this.movie.imdbId,
        overview = this.movie.overview,
        popularity = this.movie.popularity,
        tagline = this.movie.tagline,
        posterLink = this.movie.posterLink,
        backdropLink = this.movie.backdropLink,
        genres = this.genres,
        cast = this.cast,
        videos = this.videos,
        status = this.movie.status,
        releaseDate = this.movie.releaseDate,
        revenue = this.movie.revenue,
        budget = this.movie.budget,
        spokenLanguages = this.movie.spokenLanguages,
        runtime = this.movie.runtime,
        voteCount = this.movie.voteCount,
        voteAverage = this.movie.voteAverage,
        isWatchlisted = this.movie.isWatchlisted,
        recommendations = this.recommendations,
        similar = this.similar
    )
}