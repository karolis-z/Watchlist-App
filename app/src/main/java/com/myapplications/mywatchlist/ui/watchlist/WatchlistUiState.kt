package com.myapplications.mywatchlist.ui.watchlist

import com.myapplications.mywatchlist.domain.entities.TitleItem

data class WatchlistUiState(
    val titleItems: List<TitleItem>? = null,
    val isLoading: Boolean = true,
    val isNoData: Boolean = false,
)
