package com.myapplications.mywatchlist.ui.watchlist

import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.ui.components.TitleListFilter

data class WatchlistUiState(
    val titleItemsFull: List<TitleItemFull>? = null,
    val isLoading: Boolean = true,
    val isNoData: Boolean = false,
    val filter: TitleListFilter = TitleListFilter.All,
    val showSnackbar: WatchlistSnackbarType? = null
)

enum class WatchlistSnackbarType{
    NO_INTERNET
}
