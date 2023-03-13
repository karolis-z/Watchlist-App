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
    val scoreRange: Pair<Int, Int> = FilterDefaults.defaultScoreRange,
    val titleType: TitleType? = null,
    val yearsRange: Pair<Int, Int> = FilterDefaults.defaultYearsRange,
    val sortByApiParam: SortByParamUi? = null
) {
    fun getScoreRange(): ClosedFloatingPointRange<Float> =
        scoreRange.first.toFloat()..scoreRange.second.toFloat()

    fun getYearsRange(): ClosedFloatingPointRange<Float> =
        yearsRange.first.toFloat()..yearsRange.second.toFloat()

    fun getDefaultScoreRange(): ClosedFloatingPointRange<Float> =
        FilterDefaults.defaultScoreRange.first.toFloat()..FilterDefaults.defaultScoreRange.second.toFloat()

    fun getDefaultYearsRange(): ClosedFloatingPointRange<Float> =
        FilterDefaults.defaultYearsRange.first.toFloat()..FilterDefaults.defaultYearsRange.second.toFloat()

    /**
     * @return true if currently set [scoreRange] is the default range
     */
    fun isScoreRangeDefault(): Boolean = getScoreRange() == getDefaultScoreRange()

    /**
     * @return true if currently set [yearsRange] is the default range
     */
    fun isYearsRangeDefault(): Boolean = getYearsRange() == getDefaultYearsRange()

    private object FilterDefaults {
        val defaultScoreRange = Pair(0,10)
        val defaultYearsRange = Pair(1900, LocalDate.now().year)
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
