package com.myapplications.mywatchlist.ui.search

import com.myapplications.mywatchlist.domain.entities.TitleItem

data class SearchUiState(
    val titleItems: List<TitleItem>? = null,
    val isLoading: Boolean = false,
    val isNoData: Boolean = false,
    val isSearchFinished: Boolean = false
)
