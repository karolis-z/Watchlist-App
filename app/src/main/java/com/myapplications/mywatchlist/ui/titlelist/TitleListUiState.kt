package com.myapplications.mywatchlist.ui.titlelist

import androidx.paging.PagingData
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import kotlinx.coroutines.flow.Flow

sealed class TitleListUiState {
    data class Ready(val titles: Flow<PagingData<TitleItemFull>>) : TitleListUiState()
    data class Error(val error: TitleListError) : TitleListUiState()
    object Loading : TitleListUiState()
}

enum class TitleListError {
    NO_INTERNET,
    FAILED_API_REQUEST,
    NO_TITLES,
    UNKNOWN
}