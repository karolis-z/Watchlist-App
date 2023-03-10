package com.myapplications.mywatchlist.core.util

import java.util.concurrent.TimeUnit

object Constants {

    // API CONSTANTS
    const val API_KEY = "93c88359ad260c494d6a032561235630"
    const val API_BASE_URL = "https://api.themoviedb.org/3/"

    const val TMDB_IMAGES_BASE_URL = "https://image.tmdb.org/t/p/"
    const val TMDB_POSTER_SIZE_W500 = "w500"
    const val TMDB_BACKDROP_SIZE_W780 = "w780"
    const val TMDB_PROFILE_SIZE_H632 = "h632"
    const val BACKDROP_IMAGE_ASPECT_RATIO = 1.776765375854214

    // IMDB LINK BASE URL
    const val IMDB_BASE_URL = "https://www.imdb.com/title/"

    // YOUTUBE CONSTANTS
    const val YOUTUBE_WATCH_URL = "https://www.youtube.com/watch?v="
    const val YOUTUBE_APP_URI = "vnd.youtube:"
    val ACCEPTABLE_YT_ITAGS = listOf(
        18, // MP4 Audio+Video, 360p
        22, // MP4 Audio+Video, 720p
        37  // MP4 Audio+Video, 1080p
    )
    const val YOUTUBE_THUMBNAIL_BASE_URL = "https://img.youtube.com/vi/"
    const val YOUTUBE_THUMBNAIL_URL_END = "/0.jpg"
    const val YOUTUBE_THUMBNAIL_ASPECT_RATIO = 1.777777777777778
    
    // WORK REQUEST NAMES
    const val PERIODIC_WORK_REQUEST_UPDATE_CONFIGURATION = "update_configuration_periodically"

    // PAGINATION VALUES
    const val PAGE_SIZE = 20
    val CACHING_TIMEOUT = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)
}
