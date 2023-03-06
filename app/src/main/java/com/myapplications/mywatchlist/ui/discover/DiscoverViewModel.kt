package com.myapplications.mywatchlist.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
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
private const val RECENT_SEARCHES_COUNT = 15

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val titlesManager: TitlesManager
) : ViewModel() {

    private val recentSearchesFlow: StateFlow<List<String>> =
        titlesManager.getRecentSearches()
            .map { searchesList ->
                searchesList
                    .sortedByDescending { it.searchedDateTime }
                    .map { it.searchedString }
                    .take(RECENT_SEARCHES_COUNT)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
        )

    // Used to store the search string for when 'Retry' button is clicked
    private val _searchString = MutableStateFlow("")
    val searchString = _searchString.asStateFlow()

    private lateinit var titlesFlow: Flow<PagingData<TitleItemFull>>

    private val watchlistedTitles = titlesManager.allWatchlistedTitleItems()

    val searchViewState: StateFlow<SearchViewState> =
        combine(
            recentSearchesFlow,
            searchString
        ) { recentSearched, searchString ->
            if (searchString.isBlank()) {
                SearchViewState.ShowingRecent(recentSearched = recentSearched)
            } else {
                SearchViewState.Ready(titles = titlesFlow)

            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchViewState.ShowingRecent(recentSearched = recentSearchesFlow.value)
        )

    init {
        // Initializing titlesFlow
        initializeSearch()
    }

    private fun initializeSearch() {
        viewModelScope.launch {
            titlesFlow = searchString
                .debounce(400)
                .flatMapLatest { searchString ->
                    titlesManager.searchAllPaginated(query = searchString)
                        .cachedIn(viewModelScope)
                        .combine(watchlistedTitles) { pagingData, watchlistedTitles ->
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
        }
    }

    /**
     * @return [SearchViewError] based on provided [throwable]
     */
    fun getErrorFromResultThrowable(throwable: Throwable?): SearchViewError {
        return if (throwable is ApiGetTitleItemsExceptions) {
            when (throwable) {
                is ApiGetTitleItemsExceptions.FailedApiRequestException ->
                    SearchViewError.FAILED_API_REQUEST
                is ApiGetTitleItemsExceptions.NoConnectionException ->
                    SearchViewError.NO_INTERNET
                is ApiGetTitleItemsExceptions.NothingFoundException ->
                    SearchViewError.NOTHING_FOUND
            }
        } else {
            SearchViewError.UNKNOWN
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

    fun setSearchString(value: String) {
        _searchString.update { value }
    }

    fun clearSearch() {
        _searchString.update { "" }
    }

    fun retrySearch() {
        initializeSearch()
    }
}