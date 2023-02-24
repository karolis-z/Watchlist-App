package com.myapplications.mywatchlist.domain.entities

import java.time.LocalDate

/**
 * Represents the filter by which a list of TitleItems shall be fetched.
 * @param releaseDateFrom date starting from which titles shall be filtered. Null if any filter
 * starting date is OK.
 * @param releaseDateTo date ending with  titles shall be filtered. Null if any filter
 * ending date is OK.
 * @param scoreFrom indicates minimum average user score by which shall filtered. Min 0.0, Max 10.0
 * @param scoreTo indicates maximum average user score by which shall filtered. Min 0.0, Max 10.0
 * @param withGenres indicated which genres should be filtered. Provide [emptyList] if any genres are OK.
 * @param sortBy indicated how the list shall be sorted. Null - will sort by default setting. Null
 * shall also be used if sorting is not possible for a given list.
 */
data class TitleListFilter(
    val releaseDateFrom: LocalDate?,
    val releaseDateTo: LocalDate?,
    val scoreFrom: Double,
    val scoreTo: Double,
    val withGenres: List<Genre> = emptyList(),
    val sortBy: SortByParameter? = null
) {
    companion object {
        fun noConstraintsFilter() = TitleListFilter(
            releaseDateFrom = null,
            releaseDateTo = null,
            scoreFrom = 0.0,
            scoreTo = 10.0,
            withGenres = emptyList(),
            sortBy = null
        )
    }
}

interface SortByParameter {
    val propertyName: String
}