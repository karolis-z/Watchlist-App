package com.myapplications.mywatchlist.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.myapplications.mywatchlist.domain.entities.TitleItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    val viewModel = hiltViewModel<SearchViewModel>()
    val uiState = viewModel.uiState.collectAsState()

    var searchValue by remember { mutableStateOf("") }

    Column() {
        OutlinedTextField(
            value = searchValue,
            onValueChange = {
                searchValue = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 10.dp)
        )
        Button(
            onClick = { viewModel.searchTitleClicked(searchValue) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(text = "Search")
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items = uiState.value) { item: TitleItem ->
                TitleItemCard(
                    title = item,
                    onWatchlistClicked = { viewModel.onWatchlistClicked(item) },
                    onUnWatchlistClicked = { viewModel.onUnwatchlistClicked(item) })
            }
        }

    }
}

// TODO: Basic functionality to just test showing the list
@Composable
fun TitleItemCard(
    title: TitleItem,
    onWatchlistClicked: () -> Unit,
    onUnWatchlistClicked: () -> Unit,
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
        Text(text = title.overview ?: "", maxLines = 3, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = title.voteAverage.toString())
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
            Button(onClick = onWatchlistClicked) {
                Text(text = "Add to Watchlist")
            }
            Button(onClick = onUnWatchlistClicked) {
                Text(text = "UnWatchlist")
            }
        }
    }
}