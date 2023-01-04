package com.myapplications.mywatchlist.ui.watchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myapplications.mywatchlist.data.entities.TitleItemApiModel

@Composable
fun WatchlistScreen() {
    val viewModel: WatchlistViewModel = viewModel()
    val uiState = viewModel.uiState.collectAsState()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items = uiState.value) { item: TitleItemApiModel ->  
            TitleItemCard(title = item)
        }
    }
}

// TODO: Basic functionality to just test showing the list
@Composable
fun TitleItemCard(
    title: TitleItemApiModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(horizontal = 10.dp)
    ) {
        Text(text = title.name)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = title.overview ?: "")
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = title.voteAverage.toString())
    }
}