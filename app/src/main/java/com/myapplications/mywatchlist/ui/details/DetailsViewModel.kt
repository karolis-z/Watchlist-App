package com.myapplications.mywatchlist.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.domain.entities.Movie
import com.myapplications.mywatchlist.domain.repositories.DetailsRepository
import com.myapplications.mywatchlist.domain.result.ResultOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "DETAILS_VIEWMODEL"

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val detailsRepository: DetailsRepository
) : ViewModel() {

    // TODO: TEMP IMPLEMENTATION FOR TESTING
    val uiState =  MutableStateFlow<Movie?>(null)

    fun getMovie(id: Long) {
        viewModelScope.launch {
            val movieResult = detailsRepository.getMovie(id)
            if (movieResult is ResultOf.Success) {
                uiState.update { movieResult.data }
            }
        }
    }

}