package com.myapplications.mywatchlist.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.data.remote.api.TmdbApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(api: TmdbApi): ViewModel() {

    // TODO: TEMP IMPLEMENTATION FOR TESTING
    private val _uiState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val uiState = _uiState.asStateFlow()

    // TODO: TEMP IMPLEMENTATION FOR TESTING
    init {
        viewModelScope.launch {
            val apiResponse = api.search("Matrix")
            if (apiResponse.isSuccessful && apiResponse.body() != null) {
                _uiState.update { true }
            }
        }
    }

}