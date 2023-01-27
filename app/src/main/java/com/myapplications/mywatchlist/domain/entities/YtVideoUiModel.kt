package com.myapplications.mywatchlist.domain.entities

data class YtVideoUiModel(
    val link: String,
    val videoId: String,
    val name: String,
    val thumbnailLink: String,
    val videoType: YtVideoType,
    val videoFormats: List<YtVideoFormat>
)

data class YtVideoFormat(
    val downloadUrl: String,
    val itag: Int,
    val height: Int,
)