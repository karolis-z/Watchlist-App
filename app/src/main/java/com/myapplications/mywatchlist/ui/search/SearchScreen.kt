package com.myapplications.mywatchlist.ui.search

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.ui.components.LoadingCircle
import com.myapplications.mywatchlist.ui.components.TitleItemsList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel) {
    val uiState = viewModel.uiState.collectAsState()

    var searchValue by remember { mutableStateOf("") }
    val placeholderImage = if (isSystemInDarkTheme()) {
        painterResource(id = R.drawable.placeholder_poster_dark)
    } else {
        painterResource(id = R.drawable.placeholder_poster_light)
    }

    Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 5.dp)) {
        OutlinedTextField(
            value = searchValue,
            onValueChange = {
                searchValue = it
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        )
        Button(
            onClick = { viewModel.searchTitleClicked(searchValue) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Text(text = "Search")
        }
        if (!uiState.value.isSearchFinished && !uiState.value.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = stringResource(id = R.string.search_not_searched_yet),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        } else if (uiState.value.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingCircle()
            }
        } else if (uiState.value.isNoData) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.search_nothing_found),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val titleItems = uiState.value.titleItems
            if (titleItems != null) {
                TitleItemsList(
                    titleItems = titleItems,
                    placeholderImage = placeholderImage,
                    onWatchlistClicked = { viewModel.onWatchlistClicked(it) }
                )
            }
        }
    }
}

