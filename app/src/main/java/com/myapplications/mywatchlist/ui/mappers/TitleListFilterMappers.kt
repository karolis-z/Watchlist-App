package com.myapplications.mywatchlist.ui.mappers

import com.myapplications.mywatchlist.data.remote.api.SortMoviesBy
import com.myapplications.mywatchlist.data.remote.api.SortTvBy
import com.myapplications.mywatchlist.domain.entities.SortByParameter
import com.myapplications.mywatchlist.domain.entities.TitleListFilter
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.ui.entities.SortByParamUi
import com.myapplications.mywatchlist.ui.entities.TitleListUiFilter
import java.time.LocalDate

/**
 * Maps the [TitleListUiFilter] to [TitleListFilter]
 * @param sortByParameter a [SortByParameter] that indicates how the list should be sorted. Null if the title
 * list does not allow sorting.
 * @param sortByParameter if null - the default sorting shall be applied
 */
fun TitleListUiFilter.toTitleListFilter(sortByParameter: SortByParameter? = null): TitleListFilter {
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
        sortBy = sortByParameter
    )
}

/**
 * Maps the [SortByParamUi] to a [SortByParameter] of either type [SortTvBy] or [SortMoviesBy].
 * @param titleType used to determine which type of [SortByParameter] to use.
 */
fun SortByParamUi.toSortByParameter(titleType: TitleType): SortByParameter {
    return when (titleType) {
        TitleType.MOVIE -> {
            when (this) {
                SortByParamUi.Popularity_Ascending -> SortMoviesBy.Popularity_Ascending
                SortByParamUi.Popularity_Descending -> SortMoviesBy.Popularity_Descending
                SortByParamUi.ReleaseDate_Ascending -> SortMoviesBy.ReleaseDate_Ascending
                SortByParamUi.ReleaseDate_Descending -> SortMoviesBy.ReleaseDate_Descending
                SortByParamUi.Score_Ascending -> SortMoviesBy.Score_Ascending
                SortByParamUi.Score_Descending -> SortMoviesBy.Score_Descending
            }
        }
        TitleType.TV -> {
            when (this) {
                SortByParamUi.Popularity_Ascending -> SortTvBy.Popularity_Ascending
                SortByParamUi.Popularity_Descending -> SortTvBy.Popularity_Descending
                SortByParamUi.ReleaseDate_Ascending -> SortTvBy.ReleaseDate_Ascending
                SortByParamUi.ReleaseDate_Descending -> SortTvBy.ReleaseDate_Descending
                SortByParamUi.Score_Ascending -> SortTvBy.Score_Ascending
                SortByParamUi.Score_Descending -> SortTvBy.Score_Descending
            }
        }
    }
}