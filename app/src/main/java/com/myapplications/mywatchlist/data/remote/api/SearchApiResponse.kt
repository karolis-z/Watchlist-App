package com.myapplications.mywatchlist.data.remote.api

import com.myapplications.mywatchlist.domain.entities.TitleItem

data class SearchApiResponse(
    val page: Int,
    val titleItems: List<TitleItem>?,
    val totalPages: Int,
    val totalResults: Int
)
