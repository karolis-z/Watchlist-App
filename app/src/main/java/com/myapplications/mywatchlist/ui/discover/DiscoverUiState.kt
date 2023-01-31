package com.myapplications.mywatchlist.ui.discover

import com.myapplications.mywatchlist.domain.entities.TitleItemFull

data class DiscoverUiState(
    val titleItemsFull: List<TitleItemFull>? = null,
    val isLoading: Boolean = false,
    val isSearchFinished: Boolean = false,
    val error: DiscoverError? = null
)

enum class DiscoverError{
    NO_INTERNET,
    FAILED_API_REQUEST,
    NOTHING_FOUND
}