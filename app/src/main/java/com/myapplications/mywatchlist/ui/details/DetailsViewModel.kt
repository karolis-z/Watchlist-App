package com.myapplications.mywatchlist.ui.details

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.myapplications.mywatchlist.data.ApiGetDetailsException
import com.myapplications.mywatchlist.domain.entities.Movie
import com.myapplications.mywatchlist.domain.entities.TV
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import com.myapplications.mywatchlist.domain.result.ResultOf
import com.myapplications.mywatchlist.ui.NavigationArgument
import com.myapplications.mywatchlist.ui.entities.VideoItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "DETAILS_VIEWMODEL"

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val titlesManager: TitlesManager,
    val player: Player,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val uiState = MutableStateFlow(DetailsUiState())
    var videos: List<VideoItem> = emptyList()

    init {
        initializeData()
    }

    fun initializeData() {
        try {
            val titleId = savedStateHandle.get<Long>(NavigationArgument.MEDIA_ID.value)
            val titleType = savedStateHandle.get<String>(NavigationArgument.TITLE_TYPE.value)
            if (titleId == null || titleType == null) {
                // Unknown why it failed to parse the provided title, so should show general error
                uiState.update {
                    it.copy(isLoading = false, error = DetailsError.Unknown)
                }
            } else {
                getTitle(id = titleId, titleTypeString = titleType)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get title id and titletype from savedStateHandle. Reason: $e", e)
            // Unknown why it failed to parse the provided title, so should show general error
            uiState.update {
                it.copy(isLoading = false, error = DetailsError.Unknown)
            }
        }
    }

    /**
     * Retrieves a [Title] from the repository.
     */
    private fun getTitle(id: Long, titleTypeString: String) {
        val titleType = getTitleTypeFromString(titleTypeString)
        viewModelScope.launch {
            if (titleType == null) {
                uiState.update {
                    it.copy(isLoading = false, error = DetailsError.Unknown)
                }
                return@launch
            }
            val result = titlesManager.getTitle(mediaId = id, type = titleType)
            when (result) {
                is ResultOf.Failure -> {
                    uiState.update {
                        it.copy(
                            type = titleType,
                            isLoading = false,
                            error = getErrorFromResultThrowable(result.throwable)
                        )
                    }
                }
                is ResultOf.Success -> {
                    result.data.videos?.let {
                        prepareVideos(it)
                    }
                    uiState.update {
                        it.copy(
                            title = result.data,
                            type = titleType,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            }
        }
    }

    private fun getErrorFromResultThrowable(throwable: Throwable?): DetailsError {
        return if (throwable is ApiGetDetailsException) {
            when (throwable) {
                is ApiGetDetailsException.FailedApiRequestException ->
                    DetailsError.FailedApiRequest
                is ApiGetDetailsException.NoConnectionException ->
                    DetailsError.NoInternet
                // This shouldn't happen, but if it does - show a general error
                is ApiGetDetailsException.NothingFoundException ->
                    DetailsError.Unknown
            }
        } else {
            DetailsError.Unknown
        }
    }

    /**
     * Triggered when Watchlist or In Watchlist button is clicked
     */
    fun onWatchlistClicked() {
        viewModelScope.launch {
            val title = uiState.value.title
            if (title != null) {
                if (title.isWatchlisted) {
                    Log.d(TAG, "onWatchlistClicked: title IS watclisted. Unbookmarking")
                    titlesManager.unBookmarkTitle(title)
                } else {
                    Log.d(TAG, "onWatchlistClicked: title IS NOT watclisted. Bookmarking")
                    titlesManager.bookmarkTitle(title)
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
    fun convertRuntimeToHourAndMinutesPair(runtime: Int): Pair<Int, Int> {
        val hours = runtime / 60
        val minutes = runtime - (hours * 60)
        return Pair(hours, minutes)
    }

    /**
     * Prepares a list of [VideoItem]
     */
    private fun prepareVideos(videoLinks: List<String>) {
        videos = videoLinks.map { link ->
            val uri = Uri.parse(link)
            VideoItem(contentUri = uri, mediaItem = MediaItem.fromUri(uri), name = link)
        }
    }

    fun addVideoUri(uri: Uri) {
        player.addMediaItem(MediaItem.fromUri(uri))
    }

    fun playVideo(uri: Uri) {
        player.setMediaItem(
            videos.find { it.contentUri == uri }?.mediaItem ?: return
        )
        player.playWhenReady = true

        player.play()
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}