package com.myapplications.mywatchlist.ui.watchlist

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun WatchlistScreen() {
    Text(text = "Watchlist Screen")

    val viewModel = hiltViewModel<WatchlistViewModel>()
}

