package com.myapplications.mywatchlist.ui.home

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

    private val trendingDataState: MutableStateFlow<HomeUiState> =
        MutableStateFlow(HomeUiState.Loading)

    // TODO: While unnecessary now, it will later be used to combine multiple list of titles
    val uiState = trendingDataState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState.Loading
        )

    init {
        getTrendingTitles()
    }

    private fun getTrendingTitles() {
        viewModelScope.launch {
            trendingDataState.update { HomeUiState.Loading }
            val response = titlesManager.getTrendingTitles()
            when (response) {
                is ResultOf.Success -> {
                    trendingDataState.update {
                        HomeUiState.Ready(trendingItems = response.data)
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
                    trendingDataState.update {
                        HomeUiState.Error(error = error)
                    }
                }
            }
        }
    }

    /**
     * Retry getting trending titles
     */
    fun retryGetTrending() {
        getTrendingTitles()
    }

}