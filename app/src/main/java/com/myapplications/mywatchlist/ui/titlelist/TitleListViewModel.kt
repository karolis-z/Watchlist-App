package com.myapplications.mywatchlist.ui.titlelist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import com.myapplications.mywatchlist.ui.NavigationArgument
import com.myapplications.mywatchlist.ui.entities.TitleListType
import com.myapplications.mywatchlist.ui.entities.TitleListUiFilter
import com.myapplications.mywatchlist.ui.mappers.toSortByParameter
import com.myapplications.mywatchlist.ui.mappers.toTitleListFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TITLE_LIST_VIEWMODEL"

@HiltViewModel
class TitleListViewModel @Inject constructor(
    private val titlesManager: TitlesManager,
    private val genresRepository: GenresRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _screenTitle: MutableStateFlow<TitleListType?> = MutableStateFlow(null)
    val screenTitle: StateFlow<TitleListType?> = _screenTitle.asStateFlow()

    private val _titleListState: MutableStateFlow<TitleListUiState> =
        MutableStateFlow(TitleListUiState.Loading)
    val titleListState: StateFlow<TitleListUiState> = _titleListState.asStateFlow()

    private val _filterState: MutableStateFlow<TitleListUiFilter> = MutableStateFlow(TitleListUiFilter())
    val filterState = _filterState.asStateFlow()

    private val watchlistedTitles = titlesManager.allWatchlistedTitleItems()
    lateinit var titlesFlow: Flow<PagingData<TitleItemFull>>

    private var titleListType: TitleListType? = null
    private var allGenres: List<Genre>? = null

    init {
        getTitleListType()
        getTitleList()
        viewModelScope.launch {
            allGenres = genresRepository.getAvailableGenres().sortedBy { it.name }
        }
    }

    private fun getTitleListType(){
        _titleListState.update { TitleListUiState.Loading }
        try {
            val titleListTypeString = savedStateHandle.get<String>(NavigationArgument.TITLE_LIST_TYPE.value)
            if (titleListTypeString == null) {
                // Unknown why it failed to parse the provided title, so should show general error
                _titleListState.update { TitleListUiState.Error(TitleListError.UNKNOWN) }
                return
            } else {
                titleListType = TitleListType.valueOf(titleListTypeString)
                _screenTitle.update { titleListType }
                /* Setting the filter's TitleType based on list type on init. For some lists the
                title type can change by filtering, for some - it can't. */
                val titleType = when (titleListType) {
                    TitleListType.PopularMovies,
                    TitleListType.PopularTV,
                    TitleListType.TopRatedMovies,
                    TitleListType.TopRatedTV,
                    TitleListType.UpcomingMovies,
                    null -> null
                    TitleListType.DiscoverMovies -> TitleType.MOVIE
                    TitleListType.DiscoverTV -> TitleType.TV
                }
                if (titleType != null) {
                    _filterState.update { it.copy(titleType = titleType) }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get TitleListType from savedStateHandle. Reason: $e", e)
            // Unknown why it failed to parse the provided title, so should show general error
            _titleListState.update { TitleListUiState.Error(TitleListError.UNKNOWN) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getTitleList() {
        _titleListState.update { TitleListUiState.Loading }
        viewModelScope.launch {
            val listType = screenTitle.value
            if (listType != null) {
                titlesFlow = filterState.flatMapLatest { filter ->
                    when (listType) {
                        TitleListType.PopularMovies -> titlesManager.getPopularMoviesPaginated(
                            filter = filter.toTitleListFilter()
                        )
                        TitleListType.PopularTV -> titlesManager.getPopularTvPaginated(
                            filter = filter.toTitleListFilter()
                        )
                        TitleListType.TopRatedMovies -> titlesManager.getTopRatedMoviesPaginated(
                            filter = filter.toTitleListFilter()
                        )
                        TitleListType.TopRatedTV -> titlesManager.getTopRatedTVPaginated(
                            filter = filter.toTitleListFilter()
                        )
                        TitleListType.UpcomingMovies -> titlesManager.getUpcomingMoviesPaginated(
                            filter = filter.toTitleListFilter()
                        )
                        /* If the TitleListType is either one of the Discover types, then the user
                        should have the option to change to discover TV or Movies and that should be
                        set in the filterState. The initially set screenTitle will not get changed,
                        but filterState will and so we should determine which query to call based
                        on that state.
                        If listType happens to be null (but it shouldn't), default to search for
                        movies */
                        TitleListType.DiscoverMovies,
                        TitleListType.DiscoverTV -> {
                            when(filter.titleType) {
                                TitleType.MOVIE, null -> titlesManager.getDiscoverMoviesPaginated(
                                    filter = filter.toTitleListFilter(
                                        sortByParameter = filter.sortByApiParam?.toSortByParameter(
                                            titleType = TitleType.MOVIE
                                        )
                                    )
                                )
                                TitleType.TV -> titlesManager.getDiscoverTVPaginated(
                                    filter = filter.toTitleListFilter(
                                        sortByParameter = filter.sortByApiParam?.toSortByParameter(
                                            titleType = TitleType.TV
                                        )
                                    )
                                )
                            }
                        }
                    }
                }.cachedIn(viewModelScope)
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
            _titleListState.update { TitleListUiState.Ready(titles = titlesFlow) }
        }
    }

    /**
     * @return [TitleListError] based on provided [throwable]
     */
    fun getErrorFromResultThrowable(throwable: Throwable?): TitleListError {
        return if (throwable is ApiGetTitleItemsExceptions) {
            when (throwable) {
                is ApiGetTitleItemsExceptions.FailedApiRequestException ->
                    TitleListError.FAILED_API_REQUEST
                is ApiGetTitleItemsExceptions.NoConnectionException ->
                    TitleListError.NO_INTERNET
                is ApiGetTitleItemsExceptions.NothingFoundException ->
                    TitleListError.NO_TITLES
            }
        } else {
            TitleListError.UNKNOWN
        }
    }

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
     * Returns a list of all available [Genre]s for both TV and Movies
     */
    fun getAllGenres(): List<Genre> {
        return allGenres ?: emptyList()
    }

    /**
     * Updates the [_filterState] with newly selected filter.
     */
    fun setFilter(filter: TitleListUiFilter) {
        _filterState.update {
            it.copy(
                genres = filter.genres.toList(),
                scoreRange = filter.scoreRange,
                titleType = filter.titleType,
                yearsRange = filter.yearsRange,
                sortByApiParam = filter.sortByApiParam
            )
        }
    }

    /**
     * Updates the [filterState] with a newly select range of release years for the list
     */
    fun setYearsRangeFilter(range: Pair<Int, Int>) {
        _filterState.update { it.copy(yearsRange = range) }
    }

    /**
     * Updates the [filterState] with a newly select range of scores for the list
     */
    fun setScoreRangeFilter(range: Pair<Int, Int>) {
        _filterState.update { it.copy(scoreRange = range) }
    }

    /**
     * Updates the [filterState] with a newly select list of [Genre]s. Pass [emptyList] if list
     * shouldn't filter by any genre.
     */
    fun setGenresFilter(genres: List<Genre>) {
        _filterState.update { it.copy(genres = genres) }
    }

    /**
     * Updates the [filterState] with a newly select type of titles for the list
     */
    fun setTitleTypeFilter(titleType: TitleType) {
        _filterState.update { it.copy(titleType = titleType) }
    }

    /**
     * Re-initiates getting the data
     */
    fun retryGetData(){
        getTitleList()
    }
}