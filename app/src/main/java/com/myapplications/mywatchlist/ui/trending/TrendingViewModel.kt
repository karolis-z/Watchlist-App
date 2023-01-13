package com.myapplications.mywatchlist.ui.trending

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.repositories.TitlesRepository
import com.myapplications.mywatchlist.domain.result.ResultOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TRENDING_VIEWMODEL"

@HiltViewModel
class TrendingViewModel @Inject constructor(
    private val titlesRepository: TitlesRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<TrendingUiState> = MutableStateFlow(TrendingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getTrendingTitles()
    }

    private fun getTrendingTitles() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(titleItems = null, isLoading = true, error = null)
            }
            val response = titlesRepository.getTrendingTitles()
            when (response) {
                is ResultOf.Success -> {
                    _uiState.update {
                        it.copy(titleItems = response.data, isLoading = false, error = null)
                    }
                }
                is ResultOf.Failure -> {
                    val exception = response.throwable
                    val error: TrendingError =
                        if (exception is ApiGetTitleItemsExceptions) {
                            when (exception) {
                                is ApiGetTitleItemsExceptions.FailedApiRequestException,
                                is ApiGetTitleItemsExceptions.NothingFoundException ->
                                    TrendingError.FAILED_API_REQUEST
                                is ApiGetTitleItemsExceptions.NoConnectionException ->
                                    TrendingError.NO_INTERNET
                            }
                        } else {
                            TrendingError.FAILED_API_REQUEST
                        }
                    _uiState.update {
                        it.copy(titleItems = emptyList(), isLoading = false, error = error)
                    }
                }
            }
        }
    }

    fun onWatchlistClicked(title: TitleItem) {
        viewModelScope.launch {
            if (title.isWatchlisted) {
                Log.d(TAG, "onWatchlistClicked: title IS watchlisted, unbookmarking")
                titlesRepository.unBookmarkTitleItem(title)
            } else {
                Log.d(TAG, "onWatchlistClicked: title IS NOT watchlisted, bookmarking")
                titlesRepository.bookmarkTitleItem(title)
            }
        }
    }

}