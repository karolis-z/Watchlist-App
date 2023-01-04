package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.entities.TitleItemApiModel
import com.myapplications.mywatchlist.data.entities.TitleTypeApiModel
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.entities.TitleType

/**
 * Converts a [TitleItemApiModel] to [TitleItem]
 * @param allGenres a list of [Genre] from the database to map the genre ids received from api.
 */
fun TitleItemApiModel.toTitleItem(allGenres: List<Genre>): TitleItem {
    val setOfGenreIds = this.genres.map { it.toLong() }.toSet()
    return TitleItem(
        id = this.id,
        name = this.name,
        type = titleTypeApiModelToTitleType(this.type),
        mediaId = this.mediaId,
        overview = this.overview,
        posterLink = this.posterLink,
        genres = allGenres.filter { it.id in setOfGenreIds },
        releaseDate = this.releaseDate,
        voteCount = this.voteCount,
        voteAverage = this.voteAverage
    )
}

/**
 * Converts a list of [TitleItemApiModel] to a list of [TitleItem]
 * @param allGenres a list of [Genre] from the database to map the genre ids received from api.
 */
fun List<TitleItemApiModel>.toTitleItems(allGenres: List<Genre>): List<TitleItem> {
    return this.map { it.toTitleItem(allGenres) }
}

private fun titleTypeApiModelToTitleType(titleTypeApiModel: TitleTypeApiModel): TitleType {
    return when(titleTypeApiModel){
        TitleTypeApiModel.MOVIE -> TitleType.MOVIE
        TitleTypeApiModel.TV -> TitleType.TV
    }
}