package com.myapplications.mywatchlist.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import com.myapplications.mywatchlist.domain.result.ResultOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HOME_VIEWMODEL"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val titlesManager: TitlesManager
) : ViewModel() {

    private val trendingState: MutableStateFlow<TitleItemsState> =
        MutableStateFlow(TitleItemsState.Loading)
    private val upcomingMoviesState: MutableStateFlow<TitleItemsState> =
        MutableStateFlow(TitleItemsState.Loading)
    private val popularMoviesState: MutableStateFlow<TitleItemsState> =
        MutableStateFlow(TitleItemsState.Loading)
    private val popularTvState: MutableStateFlow<TitleItemsState> =
        MutableStateFlow(TitleItemsState.Loading)
    private val topRatedMoviesState: MutableStateFlow<TitleItemsState> =
        MutableStateFlow(TitleItemsState.Loading)
    private val topRatedTvState: MutableStateFlow<TitleItemsState> =
        MutableStateFlow(TitleItemsState.Loading)

    val uiState = com.myapplications.mywatchlist.core.util.extensions.combine(
        trendingState,
        upcomingMoviesState,
        popularMoviesState,
        popularTvState,
        topRatedMoviesState,
        topRatedTvState
    ) { trending, upcomingMovies, popularMovies, popularTv, topRatedMovies, topRatedTV ->
        if (trending is TitleItemsState.Loading ||
            upcomingMovies is TitleItemsState.Loading ||
            popularMovies is TitleItemsState.Loading ||
            popularTv is TitleItemsState.Loading ||
            topRatedMovies is TitleItemsState.Loading ||
            topRatedTV is TitleItemsState.Loading
        ) {
            HomeUiState.Loading
        }
        /* At this stage the logic is to show an error even if only one of the requests' responses
        were Failures. In that case, simply prioritize "NO_INTERNET" error, or show the first error */
        else if (trending is TitleItemsState.Error ||
            upcomingMovies is TitleItemsState.Error ||
            popularMovies is TitleItemsState.Error ||
            popularTv is TitleItemsState.Error ||
            topRatedMovies is TitleItemsState.Error ||
            topRatedTV is TitleItemsState.Error
        ) {
            val statesList = listOf(
                trending,
                upcomingMovies,
                popularMovies,
                popularTv,
                topRatedMovies,
                topRatedTV
            )
            HomeUiState.Error(error = determineUiStateError(statesList))
        } else {
            HomeUiState.Ready(
                trendingItems = (trending as TitleItemsState.Ready).titleItems,
                upcomingMovies = (upcomingMovies as TitleItemsState.Ready).titleItems,
                popularMovies = (popularMovies as TitleItemsState.Ready).titleItems,
                popularTV = (popularTv as TitleItemsState.Ready).titleItems,
                topRatedMovies = (topRatedMovies as TitleItemsState.Ready).titleItems,
                topRatedTV = (topRatedTV as TitleItemsState.Ready).titleItems,
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
        // Get Upcoming Movies
        getTitleItemsList(requestType = TitleItemsRequestType.UpcomingMovies)
        // Get Popular Movies
        getTitleItemsList(requestType = TitleItemsRequestType.PopularMovies)
        // Get Popular TV
        getTitleItemsList(requestType = TitleItemsRequestType.PopularTV)
        // Get Top rated movies
        getTitleItemsList(requestType = TitleItemsRequestType.TopRatedMovies)
        // Get Top rated TV
        getTitleItemsList(requestType = TitleItemsRequestType.TopRatedTV)
    }

    private fun getTitleItemsList(requestType: TitleItemsRequestType) {
        val stateToUpdate = when (requestType) {
            TitleItemsRequestType.TrendingTitles -> trendingState
            TitleItemsRequestType.UpcomingMovies -> upcomingMoviesState
            TitleItemsRequestType.PopularMovies -> popularMoviesState
            TitleItemsRequestType.PopularTV -> popularTvState
            TitleItemsRequestType.TopRatedMovies -> topRatedMoviesState
            TitleItemsRequestType.TopRatedTV -> topRatedTvState
        }
        viewModelScope.launch {
            stateToUpdate.update { TitleItemsState.Loading }
            val response = when (requestType) {
                TitleItemsRequestType.TrendingTitles -> titlesManager.getTrendingTitles()
                TitleItemsRequestType.UpcomingMovies -> titlesManager.getUpcomingMovies()
                TitleItemsRequestType.PopularMovies -> titlesManager.getPopularMovies()
                TitleItemsRequestType.PopularTV -> titlesManager.getPopularTV()
                TitleItemsRequestType.TopRatedMovies -> titlesManager.getTopRatedMovies()
                TitleItemsRequestType.TopRatedTV -> titlesManager.getTopRatedTV()
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
        object UpcomingMovies : TitleItemsRequestType()
        object PopularMovies : TitleItemsRequestType()
        object PopularTV : TitleItemsRequestType()
        object TopRatedMovies : TitleItemsRequestType()
        object TopRatedTV : TitleItemsRequestType()
    }
}