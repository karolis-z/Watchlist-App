package com.myapplications.mywatchlist.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.data.entities.TitleItemApiModel
import com.myapplications.mywatchlist.data.remote.api.TmdbApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SEARCH_VIEWMODEL"

@HiltViewModel
class SearchViewModel @Inject constructor(private val api: TmdbApi) : ViewModel() {

    // TODO: TEMP IMPLEMENTATION FOR TESTING
    private val _uiState: MutableStateFlow<List<TitleItemApiModel>> = MutableStateFlow(emptyList())
    val uiState = _uiState.asStateFlow()

    // TODO: TEMP IMPLEMENTATION FOR TESTING
    fun searchTitleClicked(query: String) {
        viewModelScope.launch {
            Log.d(TAG, "will now search for title")
            val apiResponse = api.search(query)
            Log.d(TAG, "Finished searching for title")
            if (apiResponse.isSuccessful && apiResponse.body() != null) {
                val items = apiResponse.body()!!.titleItems
                if (items != null) {
                    _uiState.update {
                        items
                    }
                }
            }
        }
    }

}