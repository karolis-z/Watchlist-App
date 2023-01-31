package com.myapplications.mywatchlist.ui.discover

import com.myapplications.mywatchlist.domain.entities.TitleItem

data class DiscoverUiState(
    val titleItems: List<TitleItem>? = null,
    val isLoading: Boolean = false,
    val isSearchFinished: Boolean = false,
    val error: DiscoverError? = null
)

enum class DiscoverError{
    NO_INTERNET,
    FAILED_API_REQUEST,
    NOTHING_FOUND
}