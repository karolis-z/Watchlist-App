package com.myapplications.mywatchlist.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import com.myapplications.mywatchlist.ui.components.TitleTypeFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "WATCHLIST_VIEWMODEL"

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val titlesManager: TitlesManager
): ViewModel() {

    private val titleFilter = MutableStateFlow(TitleTypeFilter.All)
    private val titleItemsFlow = titlesManager.allWatchlistedTitleItems()

    val uiState = combine(titleItemsFlow, titleFilter) { titleItems, titleFilter ->
        if (titleItems.isNotEmpty()) {
            val filteredList = when (titleFilter) {
                TitleTypeFilter.All -> titleItems
                TitleTypeFilter.Movies -> titleItems.filter { it.type == TitleType.MOVIE }
                TitleTypeFilter.TV -> titleItems.filter { it.type == TitleType.TV }
            }
            if (filteredList.isEmpty()) {
                WatchlistUiState(
                    titleItemsFull = null,
                    isLoading = false,
                    isNoData = true,
                    filter = titleFilter
                )
            } else {
                WatchlistUiState(
                    titleItemsFull = filteredList,
                    isLoading = false,
                    isNoData = false,
                    filter = titleFilter
                )
            }
        } else {
            WatchlistUiState(
                titleItemsFull = null,
                isLoading = false,
                isNoData = true,
                filter = titleFilter
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WatchlistUiState()
    )

    /**
     * Bookmarks or unbookmarks the chosen [TitleItemFull]
     */
    fun onWatchlistClicked(title: TitleItemFull) {
        viewModelScope.launch {
            if (title.isWatchlisted){
                titlesManager.unBookmarkTitleItem(title)
            } else {
                titlesManager.bookmarkTitleItem(title)
            }
        }
    }

    /**
     * Applies the user's chosen filter on the Trending list of Titles.
     */
    fun onTitleFilterChosen(titleTypeFilter: TitleTypeFilter){
        titleFilter.update { titleTypeFilter }
    }

}