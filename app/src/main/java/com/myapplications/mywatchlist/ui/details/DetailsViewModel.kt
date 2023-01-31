package com.myapplications.mywatchlist.ui.details

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.myapplications.mywatchlist.core.util.Constants
import com.myapplications.mywatchlist.core.util.MyYoutubeLinkExtractor
import com.myapplications.mywatchlist.core.util.capitalizedFirstLetter
import com.myapplications.mywatchlist.data.ApiGetDetailsException
import com.myapplications.mywatchlist.domain.entities.*
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import com.myapplications.mywatchlist.domain.result.ResultOf
import com.myapplications.mywatchlist.ui.NavigationArgument
import com.myapplications.mywatchlist.ui.entities.VideoItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


private const val TAG = "DETAILS_VIEWMODEL"

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val titlesManager: TitlesManager,
    val player: Player,
    private val ytLinkExtractor: MyYoutubeLinkExtractor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val uiStateInternal: MutableStateFlow<DetailsUiState> = MutableStateFlow(DetailsUiState.Loading)
    var videos: List<VideoItem> = emptyList()

    val uiState =
        combine(uiStateInternal, ytLinkExtractor.ytLinksState) { detailsUiState, ytLinksState ->
            when (detailsUiState) {
                is DetailsUiState.Error, DetailsUiState.Loading -> detailsUiState
                is DetailsUiState.Ready -> {
                    if (ytLinksState.isReady) {
                        /* Filtering the videos to only acceptable types as defined in Constants.
                        This is done with later functionality in mind to stream a better quality
                        video if better bandwidth is available and also only those that have both
                        video and audio */
                        val filteredVideosList = mutableListOf<YtVideoUiModel>()
                        ytLinksState.videos.forEach { ytVideo ->
                            val filteredVideoTypes = mutableListOf<YtVideoFormat>()
                            ytVideo.videoFormats.forEach { ytVideoType ->
                                if (Constants.ACCEPTABLE_YT_ITAGS.contains(ytVideoType.itag)) {
                                    filteredVideoTypes.add(ytVideoType)
                                }
                            }
                            if (filteredVideoTypes.isNotEmpty()) {
                                filteredVideosList.add(ytVideo.copy(videoFormats = filteredVideoTypes))
                            }
                        }
                        // Sorting by YtVideoType so that trailer is 1st, teaser 2nd and so on
                        filteredVideosList.sortBy { it.videoType.sortingOrderIndex() }
                        DetailsUiState.Ready(
                            title = detailsUiState.title,
                            type = detailsUiState.type,
                            videos = if (filteredVideosList.isEmpty()) {
                                null
                            } else {
                                filteredVideosList
                            }
                        )
                    } else {
                        DetailsUiState.Ready(
                            title = detailsUiState.title,
                            type = detailsUiState.type,
                            videos = null
                        )
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = DetailsUiState.Loading
        )

    val playerState = MutableStateFlow(Player.STATE_IDLE)

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            playerState.update { playbackState }
        }
    }

    init {
        initializeData()
        player.addListener(playerListener)
    }

    fun initializeData() {
        uiStateInternal.update { DetailsUiState.Loading }
        try {
            val titleId = savedStateHandle.get<Long>(NavigationArgument.MEDIA_ID.value)
            val titleType = savedStateHandle.get<String>(NavigationArgument.TITLE_TYPE.value)
            if (titleId == null || titleType == null) {
                // Unknown why it failed to parse the provided title, so should show general error
                uiStateInternal.update {
                    DetailsUiState.Error(DetailsError.Unknown)
                }
            } else {
                getTitle(id = titleId, titleTypeString = titleType)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get title id and titletype from savedStateHandle. Reason: $e", e)
            // Unknown why it failed to parse the provided title, so should show general error
            uiStateInternal.update {
                DetailsUiState.Error(DetailsError.Unknown)
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
                uiStateInternal.update {
                    DetailsUiState.Error(DetailsError.Unknown)
                }
                return@launch
            }
            val result = titlesManager.getTitle(mediaId = id, type = titleType)
            when (result) {
                is ResultOf.Failure -> {
                    uiStateInternal.update {
                        DetailsUiState.Error(error = getErrorFromResultThrowable(result.throwable))
                    }
                }
                is ResultOf.Success -> {
                    result.data.videos?.let {
                        extractYoutubeLinks(it)
                    }
                    uiStateInternal.update {
                        DetailsUiState.Ready(title = result.data, type = titleType)
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
            try {
                /* uiState should always be of type Ready at this stage, since function should only
                be available to be called when screen is ready and showing a "Watchlist" button */
                val readyUiState = uiStateInternal.value as DetailsUiState.Ready
                val title = readyUiState.title
                if (title.isWatchlisted) {
                    titlesManager.unBookmarkTitle(title)
                } else {
                    titlesManager.bookmarkTitle(title)
                }
                /* Updating the uiState to hold a changed value of the Title so that the correct
                state of watchlist button is shown */
                uiStateInternal.update {
                    when (readyUiState.type) {
                        TitleType.MOVIE -> {
                            readyUiState.copy(title = (title as Movie)
                                .copy(isWatchlisted = !title.isWatchlisted))
                        }
                        TitleType.TV -> {
                            readyUiState.copy(title = (title as TV)
                                .copy(isWatchlisted = !title.isWatchlisted))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "onWatchlistClicked: failed. Probably failed cast of uiState as " +
                            "DetailsUiState.Ready. Reason: $e", e)
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
     * Get a prepared spoken languages string that can be shown on screen from
     * [Title.spokenLanguages]
     */
    fun getSpokenLanguagesString(): String? {
        var languagesString = ""
        if (uiState.value !is DetailsUiState.Ready) {
            return null
        } else {
            val languages = (uiState.value as DetailsUiState.Ready).title.spokenLanguages
            languages?.let { spokenLanguages ->
                spokenLanguages.forEach { langCode ->
                    try {
                        languagesString +=
                            Locale(langCode).displayLanguage.capitalizedFirstLetter() + ", "
                        Log.d(TAG, "getSpokenLanguagesString: languagesString = $languagesString")
                    } catch (e: Exception) {
                        /* Nothing to do, just skip */
                    }
                }
                languagesString = languagesString.dropLast(2) // Dropping last space and comma
            }
            return if (languagesString == "") {
                null
            } else {
                languagesString
            }
        }
    }

    /**
     * Extracts usable YouTube links to be able to view using ExoPlayer
     * @param ytVideos a list of YouTube links in this kind of format:
     * https://www.youtube.com/watch?v=sTIBDcyCmCg
     */
    private fun extractYoutubeLinks(ytVideos: List<YtVideo>) {
        viewModelScope.launch {
            ytLinkExtractor.extractYoutubeLinks(ytVideos)
        }
    }

    fun onVideoSelected(video: YtVideoUiModel) {
        player.setMediaItem(MediaItem.fromUri(video.videoFormats[0].downloadUrl))
        player.prepare()
        player.playWhenReady = true
    }

    override fun onCleared() {
        super.onCleared()
        player.removeListener(playerListener)
        player.release()
    }
}

/**
 * Returns the sorting order index which is used to determine how the videos should be sorted in a list
 */
fun YtVideoType.sortingOrderIndex(): Int {
    return when (this) {
        YtVideoType.Trailer -> 0
        YtVideoType.Teaser -> 1
        YtVideoType.Featurette -> 2
        YtVideoType.BehindTheScenes -> 3
        YtVideoType.Other -> 4
    }
}