package com.myapplications.mywatchlist.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.myapplications.mywatchlist.core.util.Constants.PERIODIC_WORK_REQUEST_UPDATE_CONFIGURATION
import com.myapplications.mywatchlist.core.workmanager.UpdateConfigurationInfoWorker
import com.myapplications.mywatchlist.data.ApiGetGenresExceptions
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import com.myapplications.mywatchlist.domain.result.BasicResult
import com.myapplications.mywatchlist.ui.components.TitleListFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TAG = "WATCHLIST_VIEWMODEL"

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val titlesManager: TitlesManager,
    private val genresRepository: GenresRepository,
    private val workManager: WorkManager
): ViewModel() {

    private val titleFilter = MutableStateFlow(TitleListFilter.All)
    private val titleItemsFlow = titlesManager.allWatchlistedTitleItems()
    private val showSnackbarType = MutableStateFlow<WatchlistSnackbarType?>(null)

    val uiState = combine(titleItemsFlow, titleFilter, showSnackbarType) { titleItems, titleFilter, showSnackbarType ->
        if (titleItems.isNotEmpty()) {
            val filteredList = when (titleFilter) {
                TitleListFilter.All -> titleItems
                TitleListFilter.Movies -> titleItems.filter { it.type == TitleType.MOVIE }
                TitleListFilter.TV -> titleItems.filter { it.type == TitleType.TV }
            }
            if (filteredList.isEmpty()) {
                WatchlistUiState(
                    titleItemsFull = null,
                    isLoading = false,
                    isNoData = true,
                    filter = titleFilter,
                    showSnackbar = showSnackbarType
                )
            } else {
                WatchlistUiState(
                    titleItemsFull = filteredList,
                    isLoading = false,
                    isNoData = false,
                    filter = titleFilter,
                    showSnackbar = showSnackbarType
                )
            }
        } else {
            WatchlistUiState(
                titleItemsFull = null,
                isLoading = false,
                isNoData = true,
                filter = titleFilter,
                showSnackbar = showSnackbarType
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
            when (val result = genresRepository.updateGenresFromApi()) {
                is BasicResult.Failure -> {
                    if (result.exception is ApiGetGenresExceptions.NoConnectionException) {
                        showSnackbarType.update { WatchlistSnackbarType.NO_INTERNET }
                    }
                }
                is BasicResult.Success -> Unit
            }
        }
        viewModelScope.launch {
            launchPeriodicConfigurationUpdateWorker()
        }
    }

    /**
     * Reset the [showSnackbarType] to null once Snackbar has been shown.
     */
    fun resetSnackbarType() {
        showSnackbarType.update { null }
    }

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
    fun onTitleFilterChosen(titleListFilter: TitleListFilter){
        titleFilter.update { titleListFilter }
    }

    /**
     * Creates a periodic [WorkRequest] that updated the Configuration details from the api. If this
     * work had already been scheduled, then the ExistingPeriodicWorkPolicy.KEEP will ensure the
     * existing work request is kept and not replaced.
     */
    private fun launchPeriodicConfigurationUpdateWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = PeriodicWorkRequestBuilder<UpdateConfigurationInfoWorker>(3, TimeUnit.DAYS)
            .setConstraints(constraints)
            .addTag("UPDATE_CONFIGURATION")
            .build()
        workManager.enqueueUniquePeriodicWork(
            /* uniqueWorkName = */ PERIODIC_WORK_REQUEST_UPDATE_CONFIGURATION,
            /* existingPeriodicWorkPolicy = */ ExistingPeriodicWorkPolicy.KEEP,
            /* periodicWork = */ workRequest
        )
    }

}