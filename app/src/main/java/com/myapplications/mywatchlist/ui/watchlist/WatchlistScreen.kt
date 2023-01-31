package com.myapplications.mywatchlist.ui.watchlist

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
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.ui.components.FilterChipGroup
import com.myapplications.mywatchlist.ui.components.LoadingCircle
import com.myapplications.mywatchlist.ui.components.TitleItemsList

@Composable
fun WatchlistScreen(
    placeholderImage: Painter,
    onTitleClicked: (TitleItemFull) -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier
) {

    val viewModel = hiltViewModel<WatchlistViewModel>()
    val uiState = viewModel.uiState.collectAsState()

    val isLoading = uiState.value.isLoading
    val isNoData = uiState.value.isNoData
    val isTitlesAvailable = !uiState.value.titleItemsFull.isNullOrEmpty()
    val showSnackbar = uiState.value.showSnackbar

    if (showSnackbar != null) {
        when (showSnackbar) {
            WatchlistSnackbarType.NO_INTERNET -> {
                onShowSnackbar(stringResource(id = R.string.watchlist_snackbar_not_connected))
                viewModel.resetSnackbarType()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        FilterChipGroup(
            onFilterSelected = { viewModel.onTitleFilterChosen(it) },
            filter = uiState.value.filter,
            modifier = Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth()
        )
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
            visible = isNoData,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.watchlist_no_data),
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
            val titleItems = uiState.value.titleItemsFull
            if (titleItems != null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TitleItemsList(
                        titleItemsFull = titleItems,
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
