package com.myapplications.mywatchlist.ui.home

import com.myapplications.mywatchlist.domain.entities.TitleItem

data class HomeUiState(
    val titleItems: List<TitleItem>? = null,
    val isLoading: Boolean = false,
    val error: HomeError? = null
)

enum class HomeError {
    NO_INTERNET,
    FAILED_API_REQUEST,
    NO_TITLES
}
