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
import com.myapplications.mywatchlist.domain.entities.TitleItem
import com.myapplications.mywatchlist.ui.components.LoadingCircle
import com.myapplications.mywatchlist.ui.components.TitleItemsList

@Composable
fun TrendingScreen(placeholderImage: Painter, onTitleClicked: (TitleItem) -> Unit){
    val viewModel = hiltViewModel<TrendingViewModel>()

    val uiState = viewModel.uiState.collectAsState()
    val error = uiState.value.error

    if (uiState.value.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingCircle()
        }
    } else if (error != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val errorMessage = when(error){
                TrendingError.NO_INTERNET ->
                    stringResource(id = R.string.error_no_internet_connection)
                TrendingError.FAILED_API_REQUEST ->
                    stringResource(id = R.string.error_something_went_wrong)
            }
            Text(
                text = errorMessage,
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
                onTitleClicked = { onTitleClicked(it) },
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 5.dp)
            )
        }
    }
}