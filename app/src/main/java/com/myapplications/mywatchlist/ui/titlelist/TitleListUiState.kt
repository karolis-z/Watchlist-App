package com.myapplications.mywatchlist.ui.titlelist

import com.myapplications.mywatchlist.domain.entities.TitleItemFull

sealed class TitleListUiState {
    data class Ready(val titleItems: List<TitleItemFull>) : TitleListUiState()
    data class Error(val error: TitleListError) : TitleListUiState()
    object Loading : TitleListUiState()
}

enum class TitleListError {
    NO_INTERNET,
    FAILED_API_REQUEST,
    NO_TITLES,
    UNKNOWN
}