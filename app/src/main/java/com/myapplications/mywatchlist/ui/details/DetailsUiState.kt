package com.myapplications.mywatchlist.ui.details

import com.myapplications.mywatchlist.domain.entities.Title
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.domain.entities.YtVideoUiModel

sealed class DetailsUiState {

    data class Ready(
        val title: Title,
        val type: TitleType,
        val videos: List<YtVideoUiModel>? = null
    ) : DetailsUiState()

    data class Error(val error: DetailsError) : DetailsUiState()

    object Loading : DetailsUiState()
}

enum class DetailsError {
    NoInternet,
    FailedApiRequest,
    Unknown
}
