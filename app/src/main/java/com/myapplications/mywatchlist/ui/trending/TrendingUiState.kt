package com.myapplications.mywatchlist.ui.trending

import com.myapplications.mywatchlist.domain.entities.TitleItem

data class TrendingUiState(
    val titleItems: List<TitleItem>? = null,
    val isLoading: Boolean = false,
    val error: TrendingError? = null
)

enum class TrendingError {
    NO_INTERNET,
    FAILED_API_REQUEST
}
