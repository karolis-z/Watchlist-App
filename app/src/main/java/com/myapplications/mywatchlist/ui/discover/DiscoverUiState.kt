package com.myapplications.mywatchlist.ui.discover

import androidx.paging.PagingData
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.ui.entities.UiError
import kotlinx.coroutines.flow.Flow

//data class DiscoverUiState(
//    val titleItemsFull: Flow<PagingData<TitleItemFull>>? = null,
//    val isLoading: Boolean = false,
//    val isSearchFinished: Boolean = false,
//    val error: DiscoverError? = null
//)

sealed class DiscoverUiState {
    object FreshStart : DiscoverUiState()
    data class Ready(val titles: Flow<PagingData<TitleItemFull>>) : DiscoverUiState()
    object Loading : DiscoverUiState()
    data class Error(val error: DiscoverError) : DiscoverUiState()
}

enum class DiscoverError : UiError {
    NO_INTERNET,
    FAILED_API_REQUEST,
    NOTHING_FOUND,
    UNKNOWN
}