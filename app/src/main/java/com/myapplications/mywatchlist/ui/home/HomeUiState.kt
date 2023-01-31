package com.myapplications.mywatchlist.ui.home

import com.myapplications.mywatchlist.domain.entities.TitleItemFull

//data class HomeUiState(
//    val titleItemsFull: List<TitleItemFull>? = null,
//    val isLoading: Boolean = false,
//    val error: HomeError? = null
//)

sealed class HomeUiState {

    data class Ready(val trendingItems: List<TitleItemFull>) : HomeUiState()

    data class Error(val error: HomeError) : HomeUiState()

    object Loading : HomeUiState()

}

enum class HomeError {
    NO_INTERNET,
    FAILED_API_REQUEST,
    NO_TITLES
}
