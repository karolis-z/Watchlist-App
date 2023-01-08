package com.myapplications.mywatchlist.ui.watchlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.repositories.TitlesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "WATCHLIST_VIEWMODEL"

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val titlesRepository: TitlesRepository,
    private val genresRepository: GenresRepository
): ViewModel() {

    private val isLoading = MutableStateFlow(true)
    private val isNoData = MutableStateFlow(false)
    private val titleItemsFlow = titlesRepository.allWatchlistedTitleItems()
    val uiState = combine(titleItemsFlow, isLoading, isNoData) { titleItems, isLoading, isNoData ->
        Log.d(TAG, "uiState combine triggered: titleItems = $titleItems, isLoading = $isLoading, isNoData = $isNoData")
        if (titleItems.isNotEmpty()) {
            Log.d(TAG, "uiState combine: TitleItems are NOT null and NOT empty")
            WatchlistUiState(titleItems = titleItems, isLoading = false, isNoData = false)
        } else {
            Log.d(TAG, "uiState combine: TitleItems are EMPTY. Not loading, and NO DATA")
            WatchlistUiState(titleItems = null, isLoading = false, isNoData = true)
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

    // TODO: TEMP IMPLEMENTATION FOR TESTING
    fun onWatchlistClicked(title: TitleItem) {
        viewModelScope.launch {
            if (title.isWatchlisted){
                titlesRepository.unBookmarkTitle(title)
            } else {
                titlesRepository.bookmarkTitle(title)
            }
        }
    }

}