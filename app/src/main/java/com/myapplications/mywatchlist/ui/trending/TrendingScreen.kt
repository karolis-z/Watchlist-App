package com.myapplications.mywatchlist.ui.trending

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.ui.components.LoadingCircle
import com.myapplications.mywatchlist.ui.components.TitleItemsList

@Composable
fun TrendingScreen(placeholderImage: Painter){
    val viewModel = hiltViewModel<TrendingViewModel>()

    val uiState = viewModel.uiState.collectAsState()

    if (uiState.value.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingCircle()
        }
    } else if (uiState.value.isError) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.trending_error_fetching_data),
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
                onWatchlistClicked = { viewModel.onWatchlistClicked(it) },
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 5.dp)
            )
        }
    }
}