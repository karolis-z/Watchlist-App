package com.myapplications.mywatchlist.data.entities.cached

import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleType
import java.time.LocalDate

interface TitleItemCache {
    val id: Long
    val name: String
    val type: TitleType
    val mediaId: Long
    val overview: String?
    val popularity: Double?
    val posterLink: String?
    val releaseDate: LocalDate?
    val voteCount: Long
    val voteAverage: Double
    val isWatchlisted: Boolean
}

interface TitleItemCacheFull {
    val titleItem: TitleItemCache
    val genres: List<Genre>
}

interface RemoteKey {
    val cachedTitleId: Long
    val prevKey: Int?
    val currentPage: Int
    val nextKey: Int?
    val createdOn: Long
}

interface GenreForCacheItem {
    val id: Long
    val name: String
    val titleId: Long
}