package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.entities.RecentSearchEntity
import com.myapplications.mywatchlist.domain.entities.RecentSearch

/**
 * Maps a [RecentSearchEntity] to [RecentSearch]
 */
fun RecentSearchEntity.toRecentSearch(): RecentSearch {
    return RecentSearch(
        id = this.id,
        searchedString = this.searchedString,
        searchedDateTime = this.searchedDateTime
    )
}

/**
 * Maps a list of [RecentSearchEntity] to list of [RecentSearch]
 */
fun List<RecentSearchEntity>.toRecentSearchList(): List<RecentSearch> {
    return this.map { it.toRecentSearch() }
}