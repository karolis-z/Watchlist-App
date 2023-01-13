package com.myapplications.mywatchlist.ui.details

import android.util.Log
import androidx.lifecycle.SavedStateHandle
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
    private val detailsRepository: DetailsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val uiState = MutableStateFlow(DetailsUiState())

    init {
        try {
            val titleId = savedStateHandle.get<Long>("titleId")
            val titleType = savedStateHandle.get<String>("titleType")
            if (titleId == null || titleType == null) {
                uiState.update {
                    it.copy(isLoading = false, isError = true)
                }
            } else {
                getTitle(id = titleId, titleTypeString = titleType)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get title id and titletype from savedStateHandle. Reason: $e", e)
            uiState.update {
                it.copy(isLoading = false, isError = true)
            }
        }
    }

    private fun getTitle(id: Long, titleTypeString: String) {
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

    fun onWatchlistClicked() {
        viewModelScope.launch {
            val title = uiState.value.title
            if (title != null) {
                if (title.isWatchlisted) {
                    Log.d(TAG, "onWatchlistClicked: title IS watclisted. Unbookmarking")
                    detailsRepository.unBookmarkTitle(title)
                } else {
                    Log.d(TAG, "onWatchlistClicked: title IS NOT watclisted. Bookmarking")
                    detailsRepository.bookmarkTitle(title)
                }
                /* Updating the uiState to hold a changed value of the Title so that the correct
                state of watchlist button is shown */
                uiState.update {
                    when (uiState.value.type) {
                        TitleType.MOVIE -> {
                            it.copy(title = (title as Movie).copy(isWatchlisted = !title.isWatchlisted))
                        }
                        TitleType.TV -> {
                            it.copy(title = (title as TV).copy(isWatchlisted = !title.isWatchlisted))
                        }
                        /* Should never happen, but if it does, then don't do anything as we can't determine
                        * what to add to watchlist */
                        null -> return@launch
                    }
                }
            }
        }
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