package com.myapplications.mywatchlist.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import com.myapplications.mywatchlist.domain.result.ResultOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HOME_VIEWMODEL"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val titlesManager: TitlesManager
) : ViewModel() {

    private val trendingState: MutableStateFlow<TitleItemsState> =
        MutableStateFlow(TitleItemsState.Loading)
    private val popularState: MutableStateFlow<TitleItemsState> =
        MutableStateFlow(TitleItemsState.Loading)
    private val upcomingState: MutableStateFlow<TitleItemsState> =
        MutableStateFlow(TitleItemsState.Loading)
    private val topRatedState: MutableStateFlow<TitleItemsState> =
        MutableStateFlow(TitleItemsState.Loading)

    val uiState = combine(
        trendingState,
        popularState,
        upcomingState,
        topRatedState
    ) { trending, popular, upcoming, topRated ->
        if (trending is TitleItemsState.Loading ||
            popular is TitleItemsState.Loading ||
            upcoming is TitleItemsState.Loading ||
            topRated is TitleItemsState.Loading
        ) {
            HomeUiState.Loading
        }
        /* At this stage the logic is to show an error even if only one of the requests' responses
        were Failures. In that case, simply prioritize "NO_INTERNET" error, or show the first error */
        else if (trending is TitleItemsState.Error ||
            popular is TitleItemsState.Error ||
            upcoming is TitleItemsState.Error ||
            topRated is TitleItemsState.Error
        ) {
            val statesList = listOf(trending, popular)
            HomeUiState.Error(error = determineUiStateError(statesList))
        } else {
            HomeUiState.Ready(
                trendingItems = (trending as TitleItemsState.Ready).titleItems,
                popularItems = (popular as TitleItemsState.Ready).titleItems.sortedBy { it.popularity },
                upcomingItems = (upcoming as TitleItemsState.Ready).titleItems.sortedBy { it.releaseDate },
                topRatedItems = (topRated as TitleItemsState.Ready).titleItems .sortedByDescending { it.voteAverage }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

    init {
        getAllData()
    }

    private fun getAllData() {
        // Get Trending titles
        getTitleItemsList(requestType = TitleItemsRequestType.TrendingTitles)
        // Get Popular titles
        getTitleItemsList(requestType = TitleItemsRequestType.PopularTitles)
        // Get Upcoming Movies
        getTitleItemsList(requestType = TitleItemsRequestType.UpcomingMovies)
        // Get Top rated titles
        getTitleItemsList(requestType = TitleItemsRequestType.TopRatedTitles)
    }

    private fun getTitleItemsList(requestType: TitleItemsRequestType) {
        val stateToUpdate = when (requestType) {
            TitleItemsRequestType.PopularTitles -> popularState
            TitleItemsRequestType.TrendingTitles -> trendingState
            TitleItemsRequestType.TopRatedTitles -> topRatedState
            TitleItemsRequestType.UpcomingMovies -> upcomingState
        }
        viewModelScope.launch {
            stateToUpdate.update { TitleItemsState.Loading }
            val response = when (requestType) {
                TitleItemsRequestType.PopularTitles -> titlesManager.getPopularTitles()
                TitleItemsRequestType.TrendingTitles -> titlesManager.getTrendingTitles()
                TitleItemsRequestType.TopRatedTitles -> titlesManager.getTopRatedTitles()
                TitleItemsRequestType.UpcomingMovies -> titlesManager.getUpcomingMovies()
            }
            when (response) {
                is ResultOf.Success -> {
                    stateToUpdate.update {
                        TitleItemsState.Ready(titleItems = response.data)
                    }
                }
                is ResultOf.Failure -> {
                    val exception = response.throwable
                    val error: HomeError =
                        if (exception is ApiGetTitleItemsExceptions) {
                            when (exception) {
                                is ApiGetTitleItemsExceptions.FailedApiRequestException ->
                                    HomeError.FAILED_API_REQUEST
                                is ApiGetTitleItemsExceptions.NothingFoundException ->
                                    HomeError.NO_TITLES
                                is ApiGetTitleItemsExceptions.NoConnectionException ->
                                    HomeError.NO_INTERNET
                            }
                        } else {
                            HomeError.FAILED_API_REQUEST
                        }
                    stateToUpdate.update {
                        TitleItemsState.Error(error = error)
                    }
                }
            }
        }
    }

    /**
     * Determines which type of error to set for the uiState given multiple errors from the various
     * lists of TitleItems received from repositories.
     * @param statesList is a list of [TitleItemsState] of which at least one must be of type
     * [TitleItemsState.Error]
     * @throws IllegalArgumentException if [statesList] doesn't contain at least one item of type
     * [TitleItemsState.Error]
     * @return [HomeError]
     */
    private fun determineUiStateError(statesList: List<TitleItemsState>): HomeError {
        val errorStates = try {
            statesList.filterIsInstance<TitleItemsState.Error>()
        } catch (e: Exception) {
            val error = "Parameter statesList must contain at least one item of type " +
                    "TitleItemsState.Error"
            Log.e(TAG, "determineUiStateError: $error", e)
            throw IllegalArgumentException(error, e)
        }
        val errors = errorStates.map { it.error }
        /* At this stage the logic is to show an error even if only one of the requests' responses
        were Failures. In that case, simply prioritize "NO_INTERNET" error, or show the first error */
        return if (errors.contains(HomeError.NO_INTERNET)) {
            HomeError.NO_INTERNET
        } else {
            errors[0]
        }
    }

    /**
     * Retry getting trending titles
     */
    fun retryGetData() {
        getAllData()
    }

    private sealed class TitleItemsRequestType {
        object TrendingTitles : TitleItemsRequestType()
        object PopularTitles : TitleItemsRequestType()
        object TopRatedTitles : TitleItemsRequestType()
        object UpcomingMovies : TitleItemsRequestType()
    }
}