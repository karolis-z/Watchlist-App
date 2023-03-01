package com.myapplications.mywatchlist.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.myapplications.mywatchlist.ui.components.SectionHeadlineWithSeeAll
import com.myapplications.mywatchlist.ui.components.TitleItemsLazyRow
import com.myapplications.mywatchlist.ui.entities.TitleListType

@Composable
fun HomeScreen(
    placeholderPoster: Painter,
    onTitleClicked: (TitleItemFull) -> Unit,
    onSeeAllClicked: (TitleListType) -> Unit,
    modifier: Modifier
) {
    val viewModel = hiltViewModel<HomeViewModel>()

    val homeUiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Crossfade(targetState = homeUiState) { uiState ->
            when (uiState) {
                HomeUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingCircle()
                    }
                }
                is HomeUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (uiState.error) {
                            HomeError.NO_INTERNET -> {
                                val errorMessage =
                                    stringResource(id = R.string.error_no_internet_connection)
                                ErrorText(
                                    errorMessage = errorMessage,
                                    onButtonRetryClick = { viewModel.retryGetData() })
                            }
                            HomeError.FAILED_API_REQUEST -> {
                                val errorMessage =
                                    stringResource(id = R.string.error_something_went_wrong)
                                ErrorText(
                                    errorMessage = errorMessage,
                                    onButtonRetryClick = { viewModel.retryGetData() })
                            }
                            HomeError.NO_TITLES -> {
                                val errorMessage =
                                    stringResource(id = R.string.home_no_data)
                                ErrorText(errorMessage = errorMessage)
                            }
                        }
                    }
                }
                is HomeUiState.Ready -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {

                        //#region UPCOMING MOVIES
                        SectionHeadlineWithSeeAll(
                            label = stringResource(id = R.string.home_upcoming_movies_label),
                            onSeeAllClicked = { onSeeAllClicked(TitleListType.UpcomingMovies) }
                        )
                        TitleItemsLazyRow(
                            titleItemsFull = uiState.upcomingMovies,
                            placeholderPoster = placeholderPoster,
                            onTitleClicked = onTitleClicked
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        //#endregion

                        //#region POPULAR MOVIES
                        SectionHeadlineWithSeeAll(
                            label = stringResource(id = R.string.home_popular_movies_label),
                            onSeeAllClicked = { onSeeAllClicked(TitleListType.PopularMovies) }
                        )
                        TitleItemsLazyRow(
                            titleItemsFull = uiState.popularMovies,
                            placeholderPoster = placeholderPoster,
                            onTitleClicked = onTitleClicked
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        //#endregion

                        //#region POPULAR TV SHOWS
                        SectionHeadlineWithSeeAll(
                            label = stringResource(id = R.string.home_popular_tv_label),
                            onSeeAllClicked = { onSeeAllClicked(TitleListType.PopularTV) }
                        )
                        TitleItemsLazyRow(
                            titleItemsFull = uiState.popularTV,
                            placeholderPoster = placeholderPoster,
                            onTitleClicked = onTitleClicked
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        //#endregion

                        //#region TOP RATED MOVIES
                        SectionHeadlineWithSeeAll(
                            label = stringResource(id = R.string.home_top_rated_movies_label),
                            onSeeAllClicked = { onSeeAllClicked(TitleListType.TopRatedMovies) }
                        )
                        TitleItemsLazyRow(
                            titleItemsFull = uiState.topRatedMovies,
                            placeholderPoster = placeholderPoster,
                            onTitleClicked = onTitleClicked
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        //#endregion

                        //#region TOP RATED TV SHOWS
                        SectionHeadlineWithSeeAll(
                            label = stringResource(id = R.string.home_top_rated_tv_label),
                            onSeeAllClicked = { onSeeAllClicked(TitleListType.TopRatedTV) }
                        )
                        TitleItemsLazyRow(
                            titleItemsFull = uiState.topRatedTV,
                            placeholderPoster = placeholderPoster,
                            onTitleClicked = onTitleClicked
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        //#endregion
                    }
                }
            }
        }
    }
}