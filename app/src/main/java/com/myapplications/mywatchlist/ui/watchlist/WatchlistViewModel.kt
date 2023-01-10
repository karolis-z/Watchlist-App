package com.myapplications.mywatchlist.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.repositories.TitlesRepository
import com.myapplications.mywatchlist.ui.components.TitleListFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "WATCHLIST_VIEWMODEL"

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val titlesRepository: TitlesRepository,
    private val genresRepository: GenresRepository
): ViewModel() {

    private val titleFilter = MutableStateFlow(TitleListFilter.All)
    private val titleItemsFlow = titlesRepository.allWatchlistedTitleItems()

    val uiState = combine(titleItemsFlow, titleFilter) { titleItems, titleFilter ->
        if (titleItems.isNotEmpty()) {
            val filteredList = when (titleFilter) {
                TitleListFilter.All -> titleItems
                TitleListFilter.Movies -> titleItems.filter { it.type == TitleType.MOVIE }
                TitleListFilter.TV -> titleItems.filter { it.type == TitleType.TV }
            }
            if (filteredList.isEmpty()) {
                WatchlistUiState(
                    titleItems = null,
                    isLoading = false,
                    isNoData = true,
                    filter = titleFilter
                )
            } else {
                WatchlistUiState(
                    titleItems = filteredList,
                    isLoading = false,
                    isNoData = false,
                    filter = titleFilter
                )
            }
        } else {
            WatchlistUiState(
                titleItems = null,
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

    init {
        /* Initializing update of genres from the api on every new initialization of this viewmodel
        in order to make sure we always have the newest list of genres available in the database */
        viewModelScope.launch {
            genresRepository.updateGenresFromApi()
        }
    }

    /**
     * Bookmarks or unbookmarks the chosen [TitleItem]
     */
    fun onWatchlistClicked(title: TitleItem) {
        viewModelScope.launch {
            if (title.isWatchlisted){
                titlesRepository.unBookmarkTitle(title)
            } else {
                titlesRepository.bookmarkTitle(title)
            }
        }
    }

    /**
     * Applies the user's chosen filter on the Trending list of Titles.
     */
    fun onTitleFilterChosen(titleListFilter: TitleListFilter){
        titleFilter.update { titleListFilter }
    }

}