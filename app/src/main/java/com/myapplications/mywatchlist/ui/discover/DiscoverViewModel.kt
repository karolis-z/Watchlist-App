package com.myapplications.mywatchlist.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import com.myapplications.mywatchlist.ui.components.TitleTypeFilter
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

    private val _searchTitleType: MutableStateFlow<TitleTypeFilter> = MutableStateFlow(TitleTypeFilter.All)
    val searchTitleType = _searchTitleType.asStateFlow()

    /* A pair which holds both the search string and the title type filter value to have one single
    * state flow that provides all information about what search is being requested from the user */
    private val searchCriteria: StateFlow<Pair<String, TitleTypeFilter>> =
        searchString.combine(searchTitleType) { searchString, titleType ->
            Pair(searchString, titleType)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Pair("", TitleTypeFilter.All)
        )

    private lateinit var titlesFlow: Flow<PagingData<TitleItemFull>>

    private val watchlistedTitles = titlesManager.allWatchlistedTitleItems()

    val searchViewState: StateFlow<SearchViewState> =
        combine(
            recentSearchesFlow,
            searchCriteria
        ) { recentSearched, searchCriteria ->
            if (searchCriteria.first.isBlank()) {
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
            titlesFlow = searchCriteria
                .debounce(400)
                .flatMapLatest { searchCriteria ->
                    when (searchCriteria.second) {
                        TitleTypeFilter.All ->
                            titlesManager.searchAllPaginated(query = searchCriteria.first)
                        TitleTypeFilter.Movies ->
                            titlesManager.searchMoviesPaginated(query = searchCriteria.first)
                        TitleTypeFilter.TV ->
                            titlesManager.searchTVPaginated(query = searchCriteria.first)
                    }
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

    fun setTitleTypeFilter(filter: TitleTypeFilter) {
        _searchTitleType.update { filter }
    }

    fun clearSearch() {
        _searchString.update { "" }
    }

    fun onSearchClicked() {
        /* If user typed in a space (or multiple spaces) but no actual characters then we can
        * consider search not being performed and we can make the searchString empty so that
        * SearchBar shows the hint text again once search view is exited */
        if (searchString.value.isBlank() && searchString.value.isNotEmpty()) {
            _searchString.update { "" }
        }
        // Saving the most recent search to database of recent searches
        if (searchString.value.isNotBlank()) {
            viewModelScope.launch {
                titlesManager.saveNewRecentSearch(searchString.value)
            }
        }
    }
}