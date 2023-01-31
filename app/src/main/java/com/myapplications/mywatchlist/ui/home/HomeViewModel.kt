package com.myapplications.mywatchlist.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import com.myapplications.mywatchlist.domain.result.ResultOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HOME_VIEWMODEL"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val titlesManager: TitlesManager
) : ViewModel() {

    private val trendingDataState: MutableStateFlow<HomeUiState> =
        MutableStateFlow(HomeUiState.Loading)

    // TODO: Commenting out the filter for now, but will likely need it later
    //val titleFilter = MutableStateFlow(TitleListFilter.All)

    // TODO: Commenting the combine out for now, but will later need to combine different sources
    val uiState = trendingDataState
//        trendingDataState.combine(titleFilter) { uiState: HomeUiState, titleFilter: TitleListFilter ->
//            val titleItems = uiState.titleItemsFull
//            if (!titleItems.isNullOrEmpty()) {
//                val filteredList = when (titleFilter) {
//                    TitleListFilter.All -> titleItems
//                    TitleListFilter.Movies -> titleItems.filter { it.type == TitleType.MOVIE }
//                    TitleListFilter.TV -> titleItems.filter { it.type == TitleType.TV }
//                }
//                if (filteredList.isEmpty()) {
//                    HomeUiState(
//                        titleItemsFull = null,
//                        isLoading = false,
//                        error = HomeError.NO_TITLES
//                    )
//                } else {
//                    HomeUiState(titleItemsFull = filteredList, isLoading = false, error = null)
//                }
//            } else {
//                uiState
//            }
//        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState.Loading
        )

    init {
        getTrendingTitles()
    }

    private fun getTrendingTitles() {
        viewModelScope.launch {
            trendingDataState.update { HomeUiState.Loading }
            val response = titlesManager.getTrendingTitles()
            when (response) {
                is ResultOf.Success -> {
                    trendingDataState.update {
                        HomeUiState.Ready(trendingItems = response.data)
                    }
                }
                is ResultOf.Failure -> {
                    val exception = response.throwable
                    val error: HomeError =
                        if (exception is ApiGetTitleItemsExceptions) {
                            when (exception) {
                                is ApiGetTitleItemsExceptions.FailedApiRequestException ->
                                    HomeError.FAILED_API_REQUEST
                                is ApiGetTitleItemsExceptions.NothingFoundException ->
                                    HomeError.NO_TITLES
                                is ApiGetTitleItemsExceptions.NoConnectionException ->
                                    HomeError.NO_INTERNET
                            }
                        } else {
                            HomeError.FAILED_API_REQUEST
                        }
                    trendingDataState.update {
                        HomeUiState.Error(error = error)
                    }
                }
            }
        }
    }

    /**
     * Bookmarks or unbookmarks the chosen [TitleItemFull]
     */
    fun onWatchlistClicked(title: TitleItemFull) {
        viewModelScope.launch {
            if (title.isWatchlisted) {
                titlesManager.unBookmarkTitleItem(title)
            } else {
                titlesManager.bookmarkTitleItem(title)
            }
        }
    }

//    /**
//     * Applies the user's chosen filter on the Trending list of Titles.
//     */
//    fun onTitleFilterChosen(titleListFilter: TitleListFilter){
//        titleFilter.update { titleListFilter }
//    }

    /**
     * Retry getting trending titles
     */
    fun retryGetTrending() {
        getTrendingTitles()
    }

}