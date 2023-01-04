package com.myapplications.mywatchlist.data.remote.api

import com.myapplications.mywatchlist.data.entities.TitleItemApiModel

data class SearchApiResponse(
    val page: Int,
    val titleItems: List<TitleItemApiModel>?,
    val totalPages: Int,
    val totalResults: Int
)
