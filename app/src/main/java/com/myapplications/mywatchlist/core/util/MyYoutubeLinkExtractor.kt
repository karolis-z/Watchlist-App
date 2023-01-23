package com.myapplications.mywatchlist.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.SparseArray
import androidx.core.util.forEach
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

private const val TAG = "YOUTUBE_LINK_EXTRACTOR"

class MyYoutubeLinkExtractor(private val context: Context) {

//    var finalYtVideos = mutableMapOf<String, List<YtVideo>>()
    //val videos = mutableMapOf<String, List<YtVideo>>()
    
    val ytLinksState = MutableStateFlow(YtLinksState())
    private var linkCount = 0

    @SuppressLint("StaticFieldLeak")
    suspend fun extractYoutubeLinks(youtubeLinks: List<String>) {
        
        linkCount = youtubeLinks.size
        Log.d(TAG, "extractYoutubeLinks: Links Count is = $linkCount")

//        supervisorScope {
            youtubeLinks.forEachIndexed { index, link ->
//                launch {
                    // TODO: Leaving like this for now, but need to investigate a better way
                    object : YouTubeExtractor(context) {
                        override fun onExtractionComplete( ytFiles: SparseArray<YtFile>?, vMeta: VideoMeta?) {
                            val ytVideos = mutableListOf<YtVideo>()
                            ytFiles?.forEach { key, ytFile ->
                                ytVideos.add(
                                    YtVideo(
                                        downloadUrl = ytFile.url,
                                        itag = key,
                                        height = ytFile.format.height
                                    )
                                )
                                Log.d(TAG, "onExtractionComplete: just added = ${ytVideos.last()}")
                            }
                            ytLinksState.update {
                                val allVideos = it.videos.plus(Pair(link, ytVideos))
                                Log.d(TAG, "onExtractionComplete: updating the YtLinksState with these videos: $allVideos")
                                // If size is lower by 1 than link count, means we are about to add the last video list
                                if (allVideos.size == linkCount) {
                                    Log.d(TAG, "onExtractionComplete: LAST videos added.")
                                    YtLinksState(isReady = true, videos = allVideos)
                                } else {
                                    Log.d(TAG, "onExtractionComplete: NOT LAST videos added.")
                                    YtLinksState(isReady = false, videos = allVideos)
                                }
                            }
                        }
                    }.extract(link)
                }
//            }
//        }

    }
}

data class YtVideo(
    val downloadUrl: String,
    val itag: Int,
    val height: Int,
)

data class YtLinksState(
    val isReady: Boolean = false,
    val videos: Map<String, List<YtVideo>> = emptyMap()
)

