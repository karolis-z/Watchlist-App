package com.myapplications.mywatchlist.ui.watchlist

import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.ui.components.TitleListFilter

data class WatchlistUiState(
    val titleItems: List<TitleItem>? = null,
    val isLoading: Boolean = true,
    val isNoData: Boolean = false,
    val filter: TitleListFilter = TitleListFilter.All,
    val showSnackbar: WatchlistSnackbarType? = null
)

enum class WatchlistSnackbarType{
    NO_INTERNET
}
