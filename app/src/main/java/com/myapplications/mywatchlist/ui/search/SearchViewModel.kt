package com.myapplications.mywatchlist.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.domain.repositories.TitlesRepository
import com.myapplications.mywatchlist.domain.result.ResultOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SEARCH_VIEWMODEL"

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val titlesRepository: TitlesRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<SearchUiState> = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    // TODO: TEMP IMPLEMENTATION FOR TESTING
    fun searchTitleClicked(query: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    titleItems = null,
                    isLoading = true,
                    isNoData = false,
                    isSearchFinished = false
                )
            }
            val response = titlesRepository.searchTitles(query)
            when (response) {
                is ResultOf.Success -> {
                    _uiState.update {
                        it.copy(
                            titleItems = response.data,
                            isLoading = false,
                            isNoData = false,
                            isSearchFinished = true
                        )
                    }
                }
                is ResultOf.Failure -> {
                    // TODO: This should be updated for handling different reasons of failure (e.g. no internet connection)
                    _uiState.update {
                        it.copy(
                            titleItems = emptyList(),
                            isLoading = false,
                            isNoData = true,
                            isSearchFinished = true
                        )
                    }
                }
            }
        }
    }

    // TODO: TEMP IMPLEMENTATION FOR TESTING
    fun onWatchlistClicked(title: TitleItem) {
        viewModelScope.launch {
            if (title.isWatchlisted) {
                titlesRepository.unBookmarkTitle(title)
            } else {
                titlesRepository.bookmarkTitle(title)
            }
        }
    }
}