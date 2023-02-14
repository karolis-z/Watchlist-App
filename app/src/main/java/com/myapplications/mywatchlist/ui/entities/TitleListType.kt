package com.myapplications.mywatchlist.ui.entities

/**
 * Type meant for indicating which list of TitleItems should the TitleListScreen retrieve and show
 */
enum class TitleListType {
    // Trending, // Will not have Trending as a "see all" list
    PopularMovies,
    PopularTV,
    TopRatedMovies,
    TopRatedTV,
    UpcomingMovies,
    Search,
    DiscoverMovies,
    DiscoverTV
}