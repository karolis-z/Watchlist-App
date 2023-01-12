package com.myapplications.mywatchlist.ui.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.domain.entities.Movie
import com.myapplications.mywatchlist.domain.entities.Title
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

    val uiState = MutableStateFlow(DetailsUiState())

    fun getTitle(id: Long, titleTypeString: String) {
        val titleType = getTitleTypeFromString(titleTypeString)
        viewModelScope.launch {
            when(titleType){
                TitleType.MOVIE -> {
                    when(val movieResult = detailsRepository.getMovie(id)) {
                        is ResultOf.Success -> {
                            uiState.update {
                                it.copy(
                                    title = movieResult.data,
                                    type = TitleType.MOVIE,
                                    isLoading = false,
                                    isError = false
                                )
                            }
                        }
                        is ResultOf.Failure -> {
                            uiState.update {
                                it.copy(type = TitleType.MOVIE, isLoading = false, isError = true)
                            }
                        }
                    }
                }
                TitleType.TV -> {
                    when(val tvResult = detailsRepository.getTv(id)) {
                        is ResultOf.Success -> {
                            uiState.update {
                                it.copy(
                                    title = tvResult.data,
                                    type = TitleType.TV,
                                    isLoading = false,
                                    isError = false
                                )
                            }
                        }
                        is ResultOf.Failure -> {
                            uiState.update {
                                it.copy(type = TitleType.TV, isLoading = false, isError = true)
                            }
                        }
                    }
                }
                null -> {
                    uiState.update {
                        it.copy(isLoading = false, isError = true)
                    }
                }
            }
        }
    }

    fun onWatchlistClicked(title: Title) {
        //TODO
//        viewModelScope.launch {
//            if (title.isWatchlisted){
//                titlesRepository.unBookmarkTitle(title)
//            } else {
//                titlesRepository.bookmarkTitle(title)
//            }
//        }
    }

    /**
     * Gets the [TitleType] from the passed in String navigation argument.
     * @return [TitleType] if successful or null if not.
     */
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