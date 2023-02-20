package com.myapplications.mywatchlist.ui.mappers

import com.myapplications.mywatchlist.domain.entities.SortByParameter
import com.myapplications.mywatchlist.domain.entities.TitleListFilter
import com.myapplications.mywatchlist.ui.entities.TitleListUiFilter
import java.time.LocalDate

/**
 * Maps the [TitleListUiFilter] to [TitleListFilter]
 * @param sortBy a [SortByParameter] that indicates how the list should be sorted. Null if the title
 * list does not allow sorting.
 */
fun TitleListUiFilter.toTitleListFilter(sortBy: SortByParameter? = null): TitleListFilter {
    val dateFrom = if (this.yearsRange.first == 1900) {
        null
    } else {
        LocalDate.of(this.yearsRange.first, 1, 1)
    }
    val dateTo = if (this.yearsRange.second == LocalDate.now().year) {
        null
    } else {
        LocalDate.of(this.yearsRange.second, 1, 1)
    }
    return TitleListFilter(
        releaseDateFrom = if (this.yearsRange.first == 1900) {
            null
        } else {
            LocalDate.of(this.yearsRange.first, 1, 1)
        },
        releaseDateTo = if (this.yearsRange.second == LocalDate.now().year) {
            null
        } else {
            LocalDate.of(this.yearsRange.second, 1, 1)
        },
        scoreFrom = this.scoreRange.first.toDouble(),
        scoreTo = this.scoreRange.second.toDouble(),
        withGenres = this.genres,
        sortBy = sortBy
    )
}