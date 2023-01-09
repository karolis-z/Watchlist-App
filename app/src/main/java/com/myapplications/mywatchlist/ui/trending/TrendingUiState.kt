package com.myapplications.mywatchlist.ui.trending

import com.myapplications.mywatchlist.domain.entities.TitleItem

data class TrendingUiState(
    val titleItems: List<TitleItem>? = null,
    val isLoading: Boolean = false,
    val isError: Boolean = false
)
