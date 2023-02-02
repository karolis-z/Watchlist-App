package com.myapplications.mywatchlist.ui.titlelist

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.ui.components.ErrorText
import com.myapplications.mywatchlist.ui.components.LoadingCircle
import com.myapplications.mywatchlist.ui.components.TitleItemsList

@Composable
fun TitleListScreen(
    placeholderPoster: Painter,
    onTitleClicked: (TitleItemFull) -> Unit,
    modifier: Modifier = Modifier
) {

    val viewModel = hiltViewModel<TitleListViewModel>()
    val titleListUiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    )  {
        Crossfade(targetState = titleListUiState) { uiState ->
            when (uiState) {
                TitleListUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingCircle()
                    }
                }
                is TitleListUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (uiState.error) {
                            TitleListError.NO_INTERNET -> {
                                ErrorText(
                                    errorMessage =
                                        stringResource(id = R.string.error_no_internet_connection),
                                    onButtonRetryClick = { viewModel.retryGetData() }
                                )
                            }
                            TitleListError.FAILED_API_REQUEST,
                            TitleListError.UNKNOWN -> {
                                ErrorText(
                                    errorMessage =
                                        stringResource(id = R.string.error_something_went_wrong),
                                    onButtonRetryClick = { viewModel.retryGetData() }
                                )
                            }
                            TitleListError.NO_TITLES -> {
                                ErrorText(
                                    errorMessage = stringResource(id = R.string.titlelist_no_data)
                                )
                            }
                        }
                    }
                }
                is TitleListUiState.Ready -> {
                    Column(modifier = Modifier.fillMaxSize()){
                        TitleItemsList(
                            titleItemsFull = uiState.titleItems,
                            placeholderImage = placeholderPoster,
                            onWatchlistClicked = viewModel::onWatchlistClicked,
                            onTitleClicked = onTitleClicked,
                            contentPadding = PaddingValues(vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}