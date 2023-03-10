package com.myapplications.mywatchlist.ui.entities

import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleType
import java.time.LocalDate

/**
 * Describes a filter for a list of Title items.
 * @param genres list of genres to filter for. Empty list means view ALL genres.
 * @param scoreRange a pair of from-to vote average values for which to filter. 0-10 is the default.
 * @param titleType a nullable [TitleType] value. Null means the list cannot change title type and
 * will show such a filter at all.
 * @param yearsRange a pair of from-to release year values for which to filter. 1900-'this year' is the
 * default.
 */
data class TitleListUiFilter(
    val genres: List<Genre> = emptyList(),
    val scoreRange: Pair<Int, Int> = Pair(0,10),
    val titleType: TitleType? = null,
    val yearsRange: Pair<Int, Int> = Pair(1900, LocalDate.now().year),
    val sortByApiParam: SortByParamUi? = null
) {
    fun getScoreRange(): ClosedFloatingPointRange<Float> {
        return scoreRange.first.toFloat()..scoreRange.second.toFloat()
    }

    fun getYearsRange(): ClosedFloatingPointRange<Float> {
        return yearsRange.first.toFloat()..yearsRange.second.toFloat()
    }
}

enum class SortByParamUi {
    Popularity_Ascending,
    Popularity_Descending,
    ReleaseDate_Ascending,
    ReleaseDate_Descending,
    Score_Ascending,
    Score_Descending
}