package com.myapplications.mywatchlist.ui.titlelist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.myapplications.mywatchlist.core.util.Constants.PAGE_SIZE
import com.myapplications.mywatchlist.data.ApiGetDetailsException
import com.myapplications.mywatchlist.data.local.WatchlistDatabase
import com.myapplications.mywatchlist.data.local.titles.CacheDao
import com.myapplications.mywatchlist.data.mappers.toTitleItemFull
import com.myapplications.mywatchlist.data.mediators.TitlesTrendingRemoteMediator
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.repositories.TitleItemsRepository
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import com.myapplications.mywatchlist.domain.result.ResultOf
import com.myapplications.mywatchlist.ui.NavigationArgument
import com.myapplications.mywatchlist.ui.entities.TitleListType
import com.myapplications.mywatchlist.ui.entities.TitleListUiFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private const val TAG = "TITLE_LIST_VIEWMODEL"

@HiltViewModel
class TitleListViewModel @Inject constructor(
    private val titlesManager: TitlesManager,
    private val genresRepository: GenresRepository,
   // private val titlesTrendingRemoteMediator: TitlesTrendingRemoteMediator,
    private val cacheDao: CacheDao, //TODO: TEMP, should get from TitlesManager
    private val titleItemsRepository: TitleItemsRepository,
    private val db: WatchlistDatabase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _screenTitle: MutableStateFlow<TitleListType?> = MutableStateFlow(null)
    val screenTitle: StateFlow<TitleListType?> = _screenTitle.asStateFlow()

    private val titleListState: MutableStateFlow<TitleListUiState> =
        MutableStateFlow(TitleListUiState.Loading)
    private val _filterState: MutableStateFlow<TitleListUiFilter> = MutableStateFlow(TitleListUiFilter())
    val filterState = _filterState.asStateFlow()

//    @OptIn(ExperimentalPagingApi::class, FlowPreview::class)
//    val titlesTrending: Flow<PagingData<TitleItemFull>> = Pager(
//        config = PagingConfig(
//            pageSize = PAGE_SIZE,
//            prefetchDistance = PAGE_SIZE / 4,
//            initialLoadSize = PAGE_SIZE
//        ),
//        pagingSourceFactory = { cacheDao.getTrendingTitles() },
//        remoteMediator = titlesTrendingRemoteMediator
//    ).flow
//        .flowOn(Dispatchers.IO)
//        .map { pagingData ->
//            pagingData.map { titleItem ->
//                titleItem.toTitleItemFull()
//            }.filter { title -> isMatchingFilter(title, filterState.value) }
//        }
//        .cachedIn(viewModelScope)

//        .combine(filterState) { pagingData, filter ->
//            val data = pagingData.filter { title -> isMatchingFilter(title, filter) }
//            val x = mutableListOf<Long>()
//            val y = data.map {
//                 x.add(it.id)
//                 it.name
//            }
//            Log.d(TAG, "combine: $x")
//            data
//        }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalPagingApi::class)
    val titlesTrendingFlow = filterState.flatMapLatest { filter ->
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 2 * PAGE_SIZE,
                initialLoadSize = 2 * PAGE_SIZE,
                maxSize = 200
            ),
            pagingSourceFactory = { cacheDao.getTrendingTitles() },
            remoteMediator = TitlesTrendingRemoteMediator(
                titleItemsRepository = titleItemsRepository,
                database = db,
            )
        ).flow
            .map { pagingData ->
                pagingData.map {titleItem ->
                    titleItem.toTitleItemFull()
                }
            }
            .map { pagingData ->
                pagingData.filter { title -> isMatchingFilter(title, filter) }
            }
            .cachedIn(viewModelScope)
    }

    private fun isMatchingFilter(title: TitleItemFull, filter: TitleListUiFilter): Boolean {
        // Check if same type, if not return false
        if (filter.titleType != null && title.type != filter.titleType) {
            return false
        }

        // Check if within genres
        if (filter.genres.isNotEmpty()) {
            /* If none of title's genres are 'contained' within the filter's genres list, means the
            titles does not match the Genre filter and should not be shown */
            if (!title.genres.any { genre -> filter.genres.contains(genre) }){
                return false
            }
        }

        // Check if within score range
        if (title.voteAverage > filter.scoreRange.second.toDouble() ||
            title.voteAverage < filter.scoreRange.first.toDouble()) {
            return false
        }
        // Check if within years range
        val releaseYear = title.releaseDate?.year
        if (releaseYear != null) {
            // Showing future titles if the latest possible year (i.e. current) is chosen.
            val showFutureTitles = filter.yearsRange.second == LocalDate.now().year
            if (
                releaseYear.toFloat() < filter.yearsRange.first ||
                (!showFutureTitles && releaseYear.toFloat() > filter.yearsRange.second)
            ) {
                return false
            }
        }

        // If we reached this point - means all filters are 'passed' and title should be shown
        return true
    }


    val uiState: StateFlow<TitleListUiState> =
        combine(titleListState, filterState) { titleListState, filter ->
            when (titleListState) {
                is TitleListUiState.Error -> titleListState
                TitleListUiState.Loading -> titleListState
                is TitleListUiState.Ready -> {
                    TitleListUiState.Ready(
                        titleItems = filterTitles(
                            titles = titleListState.titleItems,
                            filter = filter
                        )
                    )
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

    private fun getTitleList() {
        titleListState.update { TitleListUiState.Loading }
        viewModelScope.launch {
            val result = when (titleListType) {
                null -> {
                    Log.e(TAG, "getTitleList: titleListType is null. Unable to infer which list to get")
                    titleListState.update { TitleListUiState.Error(TitleListError.UNKNOWN) }
                    return@launch
                }
                TitleListType.Trending -> titlesManager.getTrendingTitles()
                TitleListType.PopularMovies -> titlesManager.getPopularTitles()
                TitleListType.TopRatedMovies -> titlesManager.getTopRatedTitles()
                TitleListType.UpcomingMovies -> titlesManager.getUpcomingMovies()
            }
            when (result) {
                is ResultOf.Failure -> titleListState.update {
                    TitleListUiState.Error(
                        error = getErrorFromResultThrowable(
                            result.throwable
                        )
                    )
                }
                is ResultOf.Success -> titleListState.update {
                    TitleListUiState.Ready(titleItems = result.data)
                }
            }
        }
    }

    private fun getErrorFromResultThrowable(throwable: Throwable?): TitleListError {
        return if (throwable is ApiGetDetailsException) {
            when (throwable) {
                is ApiGetDetailsException.FailedApiRequestException ->
                    TitleListError.FAILED_API_REQUEST
                is ApiGetDetailsException.NoConnectionException ->
                    TitleListError.NO_INTERNET
                is ApiGetDetailsException.NothingFoundException ->
                    TitleListError.NO_TITLES
            }
        } else {
            TitleListError.UNKNOWN
        }
    }

    /**
     * Filters the given list of [TitleItemFull] by given [TitleListUiFilter]
     */
    private fun filterTitles(
        titles: List<TitleItemFull>,
        filter: TitleListUiFilter
    ): List<TitleItemFull> {

        var titlesList = titles

        // Apply TitleType filer
        if (filter.titleType != null) {
            titlesList = titlesList.filter { it.type == filter.titleType }
        }

        // Apply score range filter
        if (filter.scoreRange != Pair(0, 10)) {
            titlesList = titlesList.filter {
                (it.voteAverage >= filter.scoreRange.first) &&
                        (it.voteAverage <= filter.scoreRange.second)
            }
        }

        // Apply years range filter
        if (filter.yearsRange != Pair(1900, LocalDate.now().year)) {
            val showFutureTitles = filter.yearsRange.second == LocalDate.now().year
            val titlesWithNoReleaseDate = titlesList.filter { it.releaseDate == null }
            var titlesWithReleaseDate = if (titlesWithNoReleaseDate.isNotEmpty()) {
                titlesList.minus(titlesWithNoReleaseDate.toSet())
            } else {
                titlesList
            }
            titlesWithReleaseDate = titlesWithReleaseDate.filter {
                /* Non-null assertion here because we already filtered out items with
                null release date */
                if (showFutureTitles) {
                    it.releaseDate!!.year >= filter.yearsRange.first
                } else {
                    (it.releaseDate!!.year >= filter.yearsRange.first) &&
                            (it.releaseDate.year <= filter.yearsRange.second)
                }
            }
            titlesList = titlesWithReleaseDate + titlesWithNoReleaseDate
        }

        // Apply genres filter
        if (filter.genres.isNotEmpty()) {
            titlesList = titlesList.filter { title ->
                title.genres.any { filter.genres.contains(it) }
            }
        }

        return titlesList
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