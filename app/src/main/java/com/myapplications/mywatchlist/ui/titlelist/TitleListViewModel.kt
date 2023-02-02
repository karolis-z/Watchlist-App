package com.myapplications.mywatchlist.ui.titlelist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.data.ApiGetDetailsException
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import com.myapplications.mywatchlist.domain.result.ResultOf
import com.myapplications.mywatchlist.ui.NavigationArgument
import com.myapplications.mywatchlist.ui.entities.TitleListFilter
import com.myapplications.mywatchlist.ui.entities.TitleListType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private const val TAG = "TITLE_LIST_VIEWMODEL"

@HiltViewModel
class TitleListViewModel @Inject constructor(
    private val titlesManager: TitlesManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val titleListState: MutableStateFlow<TitleListUiState> =
        MutableStateFlow(TitleListUiState.Loading)
    private val filterState: MutableStateFlow<TitleListFilter> = MutableStateFlow(TitleListFilter())

    val uiState: StateFlow<TitleListUiState> =
        combine(titleListState, filterState) { titleListState, filter ->
            when (titleListState) {
                is TitleListUiState.Error -> titleListState
                TitleListUiState.Loading -> titleListState
                is TitleListUiState.Ready -> {
                    TitleListUiState.Ready(
                        titleItems = applyFilter(
                            titles = titleListState.titleItems,
                            filter = filter
                        )
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TitleListUiState.Loading
        )

    private var titleListType: TitleListType? = null

    init {
        getTitleListType()
        getTitleList()
    }

    private fun applyFilter(
        titles: List<TitleItemFull>,
        filter: TitleListFilter
    ): List<TitleItemFull> {
        var titlesList = titles.apply {
            // Apply TitleType filter
            if (filter.titleType != null) this.filter { it.type == filter.titleType }
            // Apply score range filter
            if (filter.scoreRange != Pair(0, 10)) this.filter {
                (it.voteAverage >= filter.scoreRange.first) &&
                        (it.voteAverage <= filter.scoreRange.second)
            }
        }
        // Apply years range filter
        if (filter.yearsRange != Pair(1900, LocalDate.now().year)) {
            val showFutureTitles = filter.scoreRange.second == LocalDate.now().year

            val titlesWithNoReleaseDate = titlesList.filter { it.releaseDate == null }
            val titlesWithReleaseDate = if (titlesWithNoReleaseDate.isNotEmpty()) {
                titlesList.minus(titlesWithNoReleaseDate.toSet())
            } else {
                titlesList
            }.filter {
                /* Non-null assertion here because we already filtered out items with
                null release date */
                if (showFutureTitles) {
                    it.releaseDate!!.year >= filter.scoreRange.first
                } else {
                    (it.releaseDate!!.year >= filter.scoreRange.first) &&
                            (it.releaseDate.year <= filter.scoreRange.second)
                }
            }
            titlesList = titlesWithReleaseDate + titlesWithNoReleaseDate
        }

        // TODO: Test this filter
        // titlesList = titlesList.filter { title -> filter.genres.any { genre -> title.genres.contains(genre) }}

        if (filter.genres.isNotEmpty()) {
            val filteredTitles = mutableListOf<TitleItemFull>()
            titlesList.forEach outer@{ title ->
                title.genres.forEach inner@{ genre ->
                    if (filter.genres.contains(genre)) {
                        filteredTitles.add(title)
                        return@outer
                    }
                }
            }
            titlesList = filteredTitles
        }
        return titlesList
    }

    private fun getTitleListType(){
        titleListState.update { TitleListUiState.Loading }
        try {
            val titleListTypeString = savedStateHandle.get<String>(NavigationArgument.TITLE_LIST_TYPE.value)
            if (titleListTypeString == null) {
                // Unknown why it failed to parse the provided title, so should show general error
                titleListState.update { TitleListUiState.Error(TitleListError.UNKNOWN) }
                return
            } else {
                titleListType = TitleListType.valueOf(titleListTypeString)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get TitleListType from savedStateHandle. Reason: $e", e)
            // Unknown why it failed to parse the provided title, so should show general error
            titleListState.update { TitleListUiState.Error(TitleListError.UNKNOWN) }
        }
    }

    private fun getTitleList() {
        titleListState.update { TitleListUiState.Loading }
        viewModelScope.launch {
            val result = when (titleListType) {
                null -> {
                    Log.e(TAG, "getTitleList: titleListType is null. Unable to infer which list to get")
                    titleListState.update { TitleListUiState.Error(TitleListError.UNKNOWN) }
                    return@launch
                }
                TitleListType.Trending -> titlesManager.getTrendingTitles()
                TitleListType.Popular -> titlesManager.getPopularTitles()
                TitleListType.TopRated -> titlesManager.getTopRatedTitles()
                TitleListType.UpcomingMovies -> titlesManager.getUpcomingMovies()
            }
            when (result) {
                is ResultOf.Failure -> titleListState.update {
                    TitleListUiState.Error(
                        error = getErrorFromResultThrowable(
                            result.throwable
                        )
                    )
                }
                is ResultOf.Success -> titleListState.update {
                    TitleListUiState.Ready(titleItems = result.data)
                }
            }
        }
    }

    private fun getErrorFromResultThrowable(throwable: Throwable?): TitleListError {
        return if (throwable is ApiGetDetailsException) {
            when (throwable) {
                is ApiGetDetailsException.FailedApiRequestException ->
                    TitleListError.FAILED_API_REQUEST
                is ApiGetDetailsException.NoConnectionException ->
                    TitleListError.NO_INTERNET
                is ApiGetDetailsException.NothingFoundException ->
                    TitleListError.NO_TITLES
            }
        } else {
            TitleListError.UNKNOWN
        }
    }

    fun onWatchlistClicked(title: TitleItemFull) {
        viewModelScope.launch {
            if (title.isWatchlisted){
                titlesManager.unBookmarkTitleItem(title)
            } else {
                titlesManager.bookmarkTitleItem(title)
            }
        }
    }

    fun retryGetData(){
        getTitleList()
    }
}