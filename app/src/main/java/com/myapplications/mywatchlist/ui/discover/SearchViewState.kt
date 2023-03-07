package com.myapplications.mywatchlist.ui.discover

import androidx.paging.PagingData
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.ui.entities.UiError
import kotlinx.coroutines.flow.Flow

sealed class SearchViewState {
    data class ShowingRecent(val recentSearched: List<String>) : SearchViewState()
    data class Ready(val titles: Flow<PagingData<TitleItemFull>>) : SearchViewState()
}

enum class SearchViewError : UiError {
    NO_INTERNET,
    FAILED_API_REQUEST,
    NOTHING_FOUND,
    UNKNOWN
}