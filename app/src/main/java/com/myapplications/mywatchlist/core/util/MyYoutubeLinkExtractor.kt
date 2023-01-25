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

    suspend fun extractYoutubeLinks(youtubeLinks: List<String>) {

        linkCount = youtubeLinks.size

        supervisorScope {
            youtubeLinks.forEach { link ->
                launch(dispatcher) {
                    val extractor = Extractor(context) { ytVideos ->
                        ytLinksState.update {
                            val videoId = link.substringAfter("watch?v=")
                            val thumbnailLink =
                                YOUTUBE_THUMBNAIL_BASE_URL + videoId + YOUTUBE_THUMBNAIL_URL_END
                            val allVideos = it.videos.plus(
                                YtVideo(
                                    link = link,
                                    videoId = videoId,
                                    /* TODO: this is temporary, just so we have unique names for now.
                                        Will need to get these names from Api later */
                                    name = link.takeLast(11),
                                    thumbnailLink = thumbnailLink,
                                    videoTypes = ytVideos
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
                    extractor.extract(link)
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    // TODO: Investigate StaticFieldLeak and how to solve this
    private inner class Extractor(
        context: Context,
        private val updateYtLinksState: (List<YtVideoType>) -> Unit
    ) : YouTubeExtractor(context) {

        override fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, videoMeta: VideoMeta?) {
            val ytVideoTypes = mutableListOf<YtVideoType>()
            ytFiles?.forEach { key, ytFile ->
                ytVideoTypes.add(
                    YtVideoType(
                        downloadUrl = ytFile.url,
                        itag = key,
                        height = ytFile.format.height
                    )
                )
            }
            updateYtLinksState(ytVideoTypes)
        }
    }
}

data class YtVideo(
    val link: String,
    val videoId: String,
    val name: String,
    val thumbnailLink: String,
    val videoTypes: List<YtVideoType>
)

data class YtVideoType(
    val downloadUrl: String,
    val itag: Int,
    val height: Int,
)

data class YtLinksState(
    val isReady: Boolean = false,
    val videos: List<YtVideo> = emptyList()
)

