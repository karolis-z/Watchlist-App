package com.myapplications.mywatchlist.domain.entities

import java.time.LocalDateTime

data class RecentSearch(
    val id: Long,
    val searchedString: String,
    val searchedDateTime: LocalDateTime
)
