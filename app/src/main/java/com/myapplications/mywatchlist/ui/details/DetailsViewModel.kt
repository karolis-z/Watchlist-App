package com.myapplications.mywatchlist.ui.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.domain.entities.Movie
import com.myapplications.mywatchlist.domain.entities.TV
import com.myapplications.mywatchlist.domain.entities.TitleType
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
    val movieUiState =  MutableStateFlow<Movie?>(null)
    val tvUiState =  MutableStateFlow<TV?>(null)

    // TODO: TEMP IMPLEMENTATION FOR TESTING
    fun getTitle(id: Long, titleTypeString: String) {
        val titleType = getTitleTypeFromString(titleTypeString)
        viewModelScope.launch {
            when(titleType){
                TitleType.MOVIE -> {
                    val movieResult = detailsRepository.getMovie(id)
                    if (movieResult is ResultOf.Success) {
                        movieUiState.update { movieResult.data }
                    }
                }
                TitleType.TV -> {
                    val tvResult = detailsRepository.getTv(id)
                    if (tvResult is ResultOf.Success) {
                        tvUiState.update { tvResult.data }
                    }
                }
                null -> Unit //TODO: need to implement proper error handling and showing on screen
            }
        }
    }

    private fun getTitleTypeFromString(titleTypeString: String): TitleType? {
        return try {
            TitleType.valueOf(titleTypeString)
        } catch (e: Exception) {
            Log.e(TAG, "getTitleTypeFromString: failed to parse passed title type string.", e)
            null
        }
    }

    /** Converts the [Movie.runtime] minutes to hours and minutes for visual representation in ui */
    fun convertRuntimeToHourAndMinutesPair(runtime: Int): Pair<Int,Int> {
        val hours = runtime / 60
        val minutes = runtime - (hours * 60)
        return Pair(hours, minutes)
    }
}