package com.myapplications.mywatchlist.ui.trending

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
                it.copy(titleItems = null, isLoading = true, isError = false)
            }
            val response = titlesRepository.getTrendingTitles()
            when (response) {
                is ResultOf.Success -> {
                    _uiState.update {
                        it.copy(titleItems = response.data, isLoading = false, isError = false)
                    }
                }
                is ResultOf.Failure -> {
                    // TODO: This should be updated for handling different reasons of failure (e.g. no internet connection)
                    _uiState.update {
                        it.copy(titleItems = emptyList(), isLoading = false, isError = true)
                    }
                }
            }
        }
    }

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