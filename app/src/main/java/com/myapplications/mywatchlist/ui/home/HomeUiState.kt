package com.myapplications.mywatchlist.ui.home

import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.ui.entities.UiError

sealed class HomeUiState {
    data class Ready(
        val trendingItems: List<TitleItemFull>,
        val upcomingMovies: List<TitleItemFull>,
        val popularMovies: List<TitleItemFull>,
        val popularTV: List<TitleItemFull>,
        val topRatedMovies: List<TitleItemFull>,
        val topRatedTV: List<TitleItemFull>
    ) : HomeUiState()
    data class Error(val error: HomeError) : HomeUiState()
    object Loading : HomeUiState()
}

sealed class TitleItemsState {
    data class Ready(val titleItems: List<TitleItemFull>) : TitleItemsState()
    data class Error(val error: HomeError) : TitleItemsState()
    object Loading : TitleItemsState()
}

enum class HomeError : UiError {
    NO_INTERNET,
    FAILED_API_REQUEST,
    NO_TITLES
}
