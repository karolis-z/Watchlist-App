package com.myapplications.mywatchlist.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.data.ApiGetTitleItemsExceptions
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import com.myapplications.mywatchlist.domain.result.ResultOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "DISCOVER_VIEWMODEL"

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val titlesManager: TitlesManager
) : ViewModel() {

    private val _uiState: MutableStateFlow<DiscoverUiState> = MutableStateFlow(DiscoverUiState())
    val uiState = _uiState.asStateFlow()

    // Used to store the search string for when 'Retry' button is clicked
    private var searchString: String = ""

    fun searchTitleClicked(query: String) {
        searchString = query
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    titleItems = null,
                    isLoading = true,
                    isSearchFinished = false,
                    error = null
                )
            }
            val response = titlesManager.searchTitles(query)
            when (response) {
                is ResultOf.Success -> {
                    _uiState.update {
                        it.copy(
                            titleItems = response.data,
                            isLoading = false,
                            isSearchFinished = true,
                            error = null
                        )
                    }
                }
                is ResultOf.Failure -> {
                    val exception = response.throwable
                    val error: DiscoverError =
                        if (exception is ApiGetTitleItemsExceptions) {
                            when (exception) {
                                is ApiGetTitleItemsExceptions.FailedApiRequestException ->
                                    DiscoverError.FAILED_API_REQUEST
                                is ApiGetTitleItemsExceptions.NothingFoundException ->
                                    DiscoverError.NOTHING_FOUND
                                is ApiGetTitleItemsExceptions.NoConnectionException ->
                                    DiscoverError.NO_INTERNET
                            }
                        } else {
                            DiscoverError.FAILED_API_REQUEST
                        }
                    _uiState.update {
                        it.copy(
                            titleItems = emptyList(),
                            isLoading = false,
                            isSearchFinished = true,
                            error = error
                        )
                    }
                }
            }
        }
    }

    fun onWatchlistClicked(title: TitleItem) {
        viewModelScope.launch {
            if (title.isWatchlisted) {
                titlesManager.unBookmarkTitleItem(title)
            } else {
                titlesManager.bookmarkTitleItem(title)
            }
        }
    }

    fun retrySearch() {
        searchTitleClicked(searchString)
    }
}