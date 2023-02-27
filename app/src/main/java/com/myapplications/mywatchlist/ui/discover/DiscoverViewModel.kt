package com.myapplications.mywatchlist.ui.discover

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "DISCOVER_VIEWMODEL"

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val titlesManager: TitlesManager
) : ViewModel() {

    private val _uiState: MutableStateFlow<DiscoverUiState> = MutableStateFlow(DiscoverUiState.FreshStart)
    val uiState = _uiState.asStateFlow()

    private val watchlistedTitles = titlesManager.allWatchlistedTitleItems()

    // Used to store the search string for when 'Retry' button is clicked
    private val _searchString = MutableStateFlow("")
    val searchString = _searchString.asStateFlow()

    fun setSearchString(value: String) {
        _searchString.update { value }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun initializeSearch() {
        _uiState.update { DiscoverUiState.Loading }
        viewModelScope.launch {
            // TODO: must adjust debounce somewhat to make it smooth. Remove logs later.
            val titles = searchString.flatMapLatest { searchString ->
                titlesManager.searchAllPaginated(query = searchString)
                    .debounce(300)
                    .cachedIn(viewModelScope)
                    .combine(watchlistedTitles) { pagingData, watchlistedTitles ->
                        Log.d(TAG, "combining search: uistate currently = ${_uiState.value}")
                        pagingData.map { pagedTitle ->
                            pagedTitle.copy(
                                isWatchlisted = watchlistedTitles.any { title ->
                                    title.mediaId == pagedTitle.mediaId &&
                                            title.type == pagedTitle.type
                                }
                            )
                        }
                    }
                    .cachedIn(viewModelScope)
            }
            if (searchString.value.isEmpty()) {
                _uiState.update { DiscoverUiState.FreshStart }
            } else {
                _uiState.update {
                    DiscoverUiState.Ready(titles = titles)
                }
            }
            Log.d(TAG, "initializeSearch: uistate updated = ${_uiState.value}")
        }
    }

    /**
     * @return [DiscoverError] based on provided [throwable]
     */
    fun getErrorFromResultThrowable(throwable: Throwable?): DiscoverError {
        return if (throwable is ApiGetTitleItemsExceptions) {
            when (throwable) {
                is ApiGetTitleItemsExceptions.FailedApiRequestException ->
                    DiscoverError.FAILED_API_REQUEST
                is ApiGetTitleItemsExceptions.NoConnectionException ->
                    DiscoverError.NO_INTERNET
                is ApiGetTitleItemsExceptions.NothingFoundException ->
                    DiscoverError.NOTHING_FOUND
            }
        } else {
            DiscoverError.UNKNOWN
        }
    }

    fun onWatchlistClicked(title: TitleItemFull) {
        viewModelScope.launch {
            if (title.isWatchlisted) {
                titlesManager.unBookmarkTitleItem(title)
            } else {
                titlesManager.bookmarkTitleItem(title)
            }
        }
    }

    fun retrySearch() {
        initializeSearch()
    }
}