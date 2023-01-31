package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.entities.TitleTypeApiModel
import com.myapplications.mywatchlist.domain.entities.TitleType

/**
 * Converts a [TitleTypeApiModel] to [TitleType]
 */
fun TitleTypeApiModel.toTitleType(): TitleType {
    return when (this) {
        TitleTypeApiModel.MOVIE -> TitleType.MOVIE
        TitleTypeApiModel.TV -> TitleType.TV
    }
}