package com.myapplications.mywatchlist.ui.watchlist

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WatchlistScreen() {
    val viewModel: WatchlistViewModel = viewModel()
    val uiState = viewModel.uiState.collectAsState()

    Text(text = "api response is successful?: $uiState")
}