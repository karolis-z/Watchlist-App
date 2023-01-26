package com.myapplications.mywatchlist.domain.entities

data class YtVideo(
    val videoId: String,
    val link: String,
    val name: String,
    val type: YtVideoType
)

enum class YtVideoType{
    Trailer,
    Teaser,
    Featurette,
    BehindTheScenes,
    Other
}