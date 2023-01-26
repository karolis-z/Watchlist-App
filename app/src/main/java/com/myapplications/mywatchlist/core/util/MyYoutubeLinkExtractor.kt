package com.myapplications.mywatchlist.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseArray
import androidx.core.util.forEach
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.myapplications.mywatchlist.core.di.DefaultDispatcher
import com.myapplications.mywatchlist.core.util.Constants.YOUTUBE_THUMBNAIL_BASE_URL
import com.myapplications.mywatchlist.core.util.Constants.YOUTUBE_THUMBNAIL_URL_END
import com.myapplications.mywatchlist.domain.entities.YtVideo
import com.myapplications.mywatchlist.domain.entities.YtVideoFormat
import com.myapplications.mywatchlist.domain.entities.YtVideoUiModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

private const val TAG = "YOUTUBE_LINK_EXTRACTOR"

class MyYoutubeLinkExtractor(
    private val context: Context,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {

    val ytLinksState = MutableStateFlow(YtLinksState())
    private var linkCount = 0

    suspend fun extractYoutubeLinks(ytVideos: List<YtVideo>) {

        linkCount = ytVideos.size

        supervisorScope {
            ytVideos.forEach { video ->
                launch(dispatcher) {
                    val extractor = Extractor(context) { ytVideos ->
                        ytLinksState.update {
                            val thumbnailLink =
                                YOUTUBE_THUMBNAIL_BASE_URL + video.videoId + YOUTUBE_THUMBNAIL_URL_END
                            val allVideos = it.videos.plus(
                                YtVideoUiModel(
                                    link = video.link,
                                    videoId = video.videoId,
                                    name = video.name,
                                    thumbnailLink = thumbnailLink,
                                    videoType = video.type,
                                    videoFormats = ytVideos
                                )
                            )
                            // If size is lower by 1 than link count, means we are about to add the last video list
                            if (allVideos.size == linkCount) {
                                YtLinksState(isReady = true, videos = allVideos)
                            } else {
                                YtLinksState(isReady = false, videos = allVideos)
                            }
                        }
                    }
                    extractor.extract(video.link)
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    // TODO: Investigate StaticFieldLeak and how to solve this
    private inner class Extractor(
        context: Context,
        private val updateYtLinksState: (List<YtVideoFormat>) -> Unit
    ) : YouTubeExtractor(context) {

        override fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, videoMeta: VideoMeta?) {
            val ytVideoFormats = mutableListOf<YtVideoFormat>()
            ytFiles?.forEach { key, ytFile ->
                ytVideoFormats.add(
                    YtVideoFormat(
                        downloadUrl = ytFile.url,
                        itag = key,
                        height = ytFile.format.height
                    )
                )
            }
            updateYtLinksState(ytVideoFormats)
        }
    }
}

data class YtLinksState(
    val isReady: Boolean = false,
    val videos: List<YtVideoUiModel> = emptyList()
)

