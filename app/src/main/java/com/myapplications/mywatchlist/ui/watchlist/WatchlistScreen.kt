package com.myapplications.mywatchlist.ui.watchlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.ui.components.TitleItemCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WatchlistScreen() {

    val viewModel = hiltViewModel<WatchlistViewModel>()
    val uiState = viewModel.uiState.collectAsState()

    val placeholderImage = if (isSystemInDarkTheme()) {
        painterResource(id = R.drawable.placeholder_poster_dark)
    } else {
        painterResource(id = R.drawable.placeholder_poster_light)
    }

    if (uiState.value.isLoading) {
        CircularProgressIndicator()
    } else if (uiState.value.isNoData) {
        Text(text = "No data")
    } else {
        val titleItems = uiState.value.titleItems
        if (titleItems != null) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 5.dp)
            ) {
                items(items = titleItems, key = { titleItem -> titleItem.id }) { item: TitleItem ->
                    TitleItemCard(
                        title = item,
                        onWatchlistClicked = { viewModel.onWatchlistClicked(item) },
                        placeholderImage = placeholderImage,
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }
}

