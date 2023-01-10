package com.myapplications.mywatchlist.ui.search

import com.myapplications.mywatchlist.domain.entities.TitleItem

data class SearchUiState(
    val titleItems: List<TitleItem>? = null,
    val isLoading: Boolean = false,
    val isSearchFinished: Boolean = false,
    val error: SearchError? = null
)

enum class SearchError{
    NO_INTERNET,
    FAILED_API_REQUEST,
    NOTHING_FOUND
}