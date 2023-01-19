package com.myapplications.mywatchlist.ui.trending

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.myapplications.mywatchlist.ui.components.FilterChipGroup
import com.myapplications.mywatchlist.ui.components.LoadingCircle
import com.myapplications.mywatchlist.ui.components.TitleItemsList

@Composable
fun TrendingScreen(
    placeholderImage: Painter,
    onTitleClicked: (TitleItem) -> Unit,
    modifier: Modifier
) {
    val viewModel = hiltViewModel<TrendingViewModel>()

    val uiState = viewModel.uiState.collectAsState()
    val filterState = viewModel.titleFilter.collectAsState()
    val error = uiState.value.error

    val isLoading = uiState.value.isLoading
    val isError = uiState.value.error != null
    val isTitlesAvailable = !uiState.value.titleItems.isNullOrEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        FilterChipGroup(
            onFilterSelected = { viewModel.onTitleFilterChosen(it) },
            filter = filterState.value,
            modifier = Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth()
        )
        //TODO: Consider switching to AnimatedContent?
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingCircle()
            }
        }
        AnimatedVisibility(
            visible = isError,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val errorMessage = when (error) {
                    TrendingError.NO_INTERNET ->
                        stringResource(id = R.string.error_no_internet_connection)
                    TrendingError.FAILED_API_REQUEST ->
                        stringResource(id = R.string.error_something_went_wrong)
                    TrendingError.NO_TITLES ->
                        stringResource(id = R.string.trending_nothing_trending)
                    null -> "" // This should never happen because isError already controls for this
                }
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
        AnimatedVisibility(
            visible = isTitlesAvailable,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val titleItems = uiState.value.titleItems
            if (titleItems != null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TitleItemsList(
                        titleItems = titleItems,
                        placeholderImage = placeholderImage,
                        onWatchlistClicked = { viewModel.onWatchlistClicked(it) },
                        onTitleClicked = { onTitleClicked(it) },
                        contentPadding = PaddingValues(vertical = 10.dp)
                    )
                }
            }
        }
    }
}