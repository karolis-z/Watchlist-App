package com.myapplications.mywatchlist.ui.titlelist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapplications.mywatchlist.data.ApiGetDetailsException
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import com.myapplications.mywatchlist.domain.result.ResultOf
import com.myapplications.mywatchlist.ui.NavigationArgument
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TITLE_LIST_VIEWMODEL"

@HiltViewModel
class TitleListViewModel @Inject constructor(
    private val titlesManager: TitlesManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val uiState: MutableStateFlow<TitleListUiState> = MutableStateFlow(TitleListUiState.Loading)

    private var titleListType: TitleListType? = null

    init {
        getTitleListType()
        getTitleList()
    }

    private fun getTitleListType(){
        uiState.update { TitleListUiState.Loading }
        try {
            val titleListTypeString = savedStateHandle.get<String>(NavigationArgument.TITLE_LIST_TYPE.value)
            if (titleListTypeString == null) {
                // Unknown why it failed to parse the provided title, so should show general error
                uiState.update { TitleListUiState.Error(TitleListError.UNKNOWN) }
                return
            } else {
                titleListType = TitleListType.valueOf(titleListTypeString)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get TitleListType from savedStateHandle. Reason: $e", e)
            // Unknown why it failed to parse the provided title, so should show general error
            uiState.update { TitleListUiState.Error(TitleListError.UNKNOWN) }
        }
    }

    private fun getTitleList() {
        uiState.update { TitleListUiState.Loading }
        viewModelScope.launch {
            val result = when (titleListType) {
                null -> {
                    Log.e(TAG, "getTitleList: titleListType is null. Unable to infer which list to get")
                    uiState.update { TitleListUiState.Error(TitleListError.UNKNOWN) }
                    return@launch
                }
                TitleListType.Trending -> titlesManager.getTrendingTitles()
                TitleListType.Popular -> titlesManager.getPopularTitles()
                TitleListType.TopRated -> titlesManager.getTopRatedTitles()
                TitleListType.UpcomingMovies -> titlesManager.getUpcomingMovies()
            }
            when (result) {
                is ResultOf.Failure -> uiState.update {
                    TitleListUiState.Error(
                        error = getErrorFromResultThrowable(
                            result.throwable
                        )
                    )
                }
                is ResultOf.Success -> uiState.update {
                    TitleListUiState.Ready(titleItems = result.data)
                }
            }
        }
    }

    private fun getErrorFromResultThrowable(throwable: Throwable?): TitleListError {
        return if (throwable is ApiGetDetailsException) {
            when (throwable) {
                is ApiGetDetailsException.FailedApiRequestException ->
                    TitleListError.FAILED_API_REQUEST
                is ApiGetDetailsException.NoConnectionException ->
                    TitleListError.NO_INTERNET
                is ApiGetDetailsException.NothingFoundException ->
                    TitleListError.NO_TITLES
            }
        } else {
            TitleListError.UNKNOWN
        }
    }

    fun onWatchlistClicked(title: TitleItemFull) {
        viewModelScope.launch {
            if (title.isWatchlisted){
                titlesManager.unBookmarkTitleItem(title)
            } else {
                titlesManager.bookmarkTitleItem(title)
            }
        }
    }

    fun retryGetData(){
        getTitleList()
    }
}

enum class TitleListType {
    Trending,
    Popular,
    TopRated,
    UpcomingMovies
}