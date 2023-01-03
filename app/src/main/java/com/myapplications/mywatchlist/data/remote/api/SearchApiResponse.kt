package com.myapplications.mywatchlist.data.remote.api

import com.myapplications.mywatchlist.data.entities.TitleItem

data class SearchApiResponse(
    val page: Int,
    val titleItems: List<TitleItem>?,
    val totalPages: Int,
    val totalResults: Int
)
