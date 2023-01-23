package com.myapplications.mywatchlist.ui.details

import com.myapplications.mywatchlist.core.util.YtVideo
import com.myapplications.mywatchlist.domain.entities.Title
import com.myapplications.mywatchlist.domain.entities.TitleType

data class DetailsUiState(
    val title: Title? = null,
    val type: TitleType? = null,
    val isLoading: Boolean = true,
    val error: DetailsError? = null,
    val videos: Map<String, List<YtVideo>>? = null
)

enum class DetailsError {
    NoInternet,
    FailedApiRequest,
    Unknown
}
