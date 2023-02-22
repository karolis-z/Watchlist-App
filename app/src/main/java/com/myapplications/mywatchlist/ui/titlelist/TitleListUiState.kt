package com.myapplications.mywatchlist.ui.titlelist

sealed class TitleListUiState {
    object Ready : TitleListUiState()
    data class Error(val error: TitleListError) : TitleListUiState()
    object Loading : TitleListUiState()
}

enum class TitleListError {
    NO_INTERNET,
    FAILED_API_REQUEST,
    NO_TITLES,
    UNKNOWN
}