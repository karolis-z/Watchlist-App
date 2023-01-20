package com.myapplications.mywatchlist.ui.trending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import com.myapplications.mywatchlist.domain.result.ResultOf
import com.myapplications.mywatchlist.ui.components.TitleListFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TRENDING_VIEWMODEL"

@HiltViewModel
class TrendingViewModel @Inject constructor(
    private val titlesManager: TitlesManager
) : ViewModel() {

    private val trendingDataState: MutableStateFlow<TrendingUiState> =
        MutableStateFlow(TrendingUiState())

    val titleFilter = MutableStateFlow(TitleListFilter.All)

    val uiState =
        trendingDataState.combine(titleFilter) { uiState: TrendingUiState, titleFilter: TitleListFilter ->
            val titleItems = uiState.titleItems
            if (!titleItems.isNullOrEmpty()) {
                val filteredList = when (titleFilter) {
                    TitleListFilter.All -> titleItems
                    TitleListFilter.Movies -> titleItems.filter { it.type == TitleType.MOVIE }
                    TitleListFilter.TV -> titleItems.filter { it.type == TitleType.TV }
                }
                if (filteredList.isEmpty()) {
                    TrendingUiState(
                        titleItems = null,
                        isLoading = false,
                        error = TrendingError.NO_TITLES
                    )
                } else {
                    TrendingUiState(titleItems = filteredList, isLoading = false, error = null)
                }
            } else {
                uiState
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TrendingUiState()
        )

    init {
        getTrendingTitles()
    }

    private fun getTrendingTitles() {
        viewModelScope.launch {
            trendingDataState.update {
                it.copy(isLoading = true)
            }
            val response = titlesManager.getTrendingTitles()
            when (response) {
                is ResultOf.Success -> {
                    trendingDataState.update {
                        it.copy(titleItems = response.data, isLoading = false, error = null)
                    }
                }
                is ResultOf.Failure -> {
                    val exception = response.throwable
                    val error: TrendingError =
                        if (exception is ApiGetTitleItemsExceptions) {
                            when (exception) {
                                is ApiGetTitleItemsExceptions.FailedApiRequestException ->
                                    TrendingError.FAILED_API_REQUEST
                                is ApiGetTitleItemsExceptions.NothingFoundException ->
                                    TrendingError.NO_TITLES
                                is ApiGetTitleItemsExceptions.NoConnectionException ->
                                    TrendingError.NO_INTERNET
                            }
                        } else {
                            TrendingError.FAILED_API_REQUEST
                        }
                    trendingDataState.update {
                        it.copy(titleItems = emptyList(), isLoading = false, error = error)
                    }
                }
            }
        }
    }

    /**
     * Bookmarks or unbookmarks the chosen [TitleItem]
     */
    fun onWatchlistClicked(title: TitleItem) {
        viewModelScope.launch {
            if (title.isWatchlisted) {
                titlesManager.unBookmarkTitleItem(title)
            } else {
                titlesManager.bookmarkTitleItem(title)
            }
        }
    }

    /**
     * Applies the user's chosen filter on the Trending list of Titles.
     */
    fun onTitleFilterChosen(titleListFilter: TitleListFilter){
        titleFilter.update { titleListFilter }
    }

    /**
     * Retry getting trending titles
     */
    fun retryGetTrending() {
        getTrendingTitles()
    }

}