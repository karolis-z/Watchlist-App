package com.myapplications.mywatchlist.ui.search

import android.util.Log
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

    // TODO: TEMP IMPLEMENTATION FOR TESTING
    private val _uiState: MutableStateFlow<List<TitleItem>> = MutableStateFlow(emptyList())
    val uiState = _uiState.asStateFlow()

    // TODO: TEMP IMPLEMENTATION FOR TESTING
    fun searchTitleClicked(query: String) {
        viewModelScope.launch {
            Log.d(TAG, "will now search for title")
            val response = titlesRepository.searchTitles(query)
            Log.d(TAG, "Finished searching for title")
            Log.d(TAG, "searchTitleClicked: apiResponse = ${response}")
            if (response is ResultOf.Success) {
                val items = response.data
                _uiState.update { items }
            }
        }
    }

    // TODO: TEMP IMPLEMENTATION FOR TESTING
    fun onWatchlistClicked(title: TitleItem) {
        viewModelScope.launch {
            if (title.isWatchlisted){
                titlesRepository.unBookmarkTitle(title)
            } else {
                titlesRepository.bookmarkTitle(title)
            }
        }
    }
}