package com.myapplications.mywatchlist.ui.titlelist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import com.myapplications.mywatchlist.ui.NavigationArgument
import com.myapplications.mywatchlist.ui.entities.TitleListType
import com.myapplications.mywatchlist.ui.entities.TitleListUiFilter
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

    private val titleListState: MutableStateFlow<TitleListUiState> =
        MutableStateFlow(TitleListUiState.Loading)
    private val _filterState: MutableStateFlow<TitleListUiFilter> = MutableStateFlow(TitleListUiFilter())
    val filterState = _filterState.asStateFlow()

    lateinit var titlesFlow: Flow<PagingData<TitleItemFull>>

    val uiState: StateFlow<TitleListUiState> =
        combine(titleListState, filterState) { titleListState, filter ->
            when (titleListState) {
                is TitleListUiState.Error -> titleListState
                TitleListUiState.Loading -> titleListState
                is TitleListUiState.Ready -> {
                    TitleListUiState.Ready
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TitleListUiState.Loading
        )

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
        titleListState.update { TitleListUiState.Loading }
        try {
            val titleListTypeString = savedStateHandle.get<String>(NavigationArgument.TITLE_LIST_TYPE.value)
            if (titleListTypeString == null) {
                // Unknown why it failed to parse the provided title, so should show general error
                titleListState.update { TitleListUiState.Error(TitleListError.UNKNOWN) }
                return
            } else {
                titleListType = TitleListType.valueOf(titleListTypeString)
                _screenTitle.update { titleListType }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get TitleListType from savedStateHandle. Reason: $e", e)
            // Unknown why it failed to parse the provided title, so should show general error
            titleListState.update { TitleListUiState.Error(TitleListError.UNKNOWN) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getTitleList() {
        titleListState.update { TitleListUiState.Loading }
        viewModelScope.launch {
            val listType = screenTitle.value
            if (listType != null) {
                titlesFlow = filterState.flatMapLatest { filter ->
                    Log.d(TAG, "getTitleList: listtype = $listType. filter set: $filter")
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
                    }
                }.cachedIn(viewModelScope)
            }
            titleListState.update { TitleListUiState.Ready }
        }
    }

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
                yearsRange = filter.yearsRange
            )
        }
    }

    fun retryGetData(){
        getTitleList()
    }
}