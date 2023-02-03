package com.myapplications.mywatchlist.ui.titlelist

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.ui.components.ErrorText
import com.myapplications.mywatchlist.ui.components.LoadingCircle
import com.myapplications.mywatchlist.ui.components.TitleItemsList
import com.myapplications.mywatchlist.ui.entities.TitleListFilter
import kotlin.math.roundToInt

@Composable
fun TitleListScreen(
    placeholderPoster: Painter,
    onTitleClicked: (TitleItemFull) -> Unit,
    modifier: Modifier = Modifier
) {

    val viewModel = hiltViewModel<TitleListViewModel>()
    val titleListUiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

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
                        //#region FILTERS SECTION. WILL BE UPDATED WHEN DESIGNED FINISHES THE UI/UX
                        val defaultFilter = TitleListFilter()
                        FilterSection(
                            filter = filterState,
                            defaultScoreRange = defaultFilter.scoreRange.first.toFloat()..defaultFilter.scoreRange.second.toFloat(),
                            defaultYearsRange = defaultFilter.yearsRange.first.toFloat()..defaultFilter.yearsRange.second.toFloat(),
                            allGenres = viewModel.getAllGenres(),
                            onFilterApplied = viewModel::setFilter
                        )
                        //#endregion


                        //#region TITLE ITEMS LIST
                        TitleItemsList(
                            titleItemsFull = uiState.titleItems,
                            placeholderImage = placeholderPoster,
                            onWatchlistClicked = viewModel::onWatchlistClicked,
                            onTitleClicked = onTitleClicked,
                            contentPadding = PaddingValues(vertical = 10.dp)
                        )
                        //#endregion
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSection(
    filter: TitleListFilter,
    defaultScoreRange: ClosedFloatingPointRange<Float>,
    defaultYearsRange: ClosedFloatingPointRange<Float>,
    allGenres: List<Genre>,
    onFilterApplied: (TitleListFilter) -> Unit,
    modifier: Modifier = Modifier
) {

    var scoreRange by remember {
        mutableStateOf(filter.scoreRange.first.toFloat()..filter.scoreRange.second.toFloat())
    }
    var yearsRange by remember {
        mutableStateOf(filter.yearsRange.first.toFloat()..filter.yearsRange.second.toFloat())
    }
    val selectedGenres = remember { mutableStateListOf<Genre>().apply {
        addAll(filter.genres)
    } }
    val scoreFromText by remember { derivedStateOf { "From: ${scoreRange.start.roundToInt()}" } }
    val scoreToText by remember { derivedStateOf { "To: ${scoreRange.endInclusive.roundToInt()}" } }
    val yearFromText by remember { derivedStateOf { "From: ${yearsRange.start.roundToInt()}" } }
    val yearToText by remember { derivedStateOf { "To: ${yearsRange.endInclusive.roundToInt()}" } }

    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(text = "Type", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 5.dp, end = 10.dp))
            // TODO: Add type of title
        }

        // Score Slider
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(text = "Score", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 5.dp, end = 10.dp))
            Text(text = scoreFromText, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(end = 10.dp))
            Text(text = scoreToText, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(end = 10.dp))
        }
        RangeSlider(
            value = scoreRange,
            onValueChange = {
                scoreRange = it
            },
            valueRange = defaultScoreRange,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Years Slider
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(text = "Release Year", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 5.dp, end = 10.dp))
            Text(text = yearFromText, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(end = 10.dp))
            Text(text = yearToText, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(end = 10.dp))
        }
        RangeSlider(
            value = yearsRange,
            onValueChange = {
                yearsRange = it
            },
            valueRange = defaultYearsRange,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        
        // Genres Flow
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(text = "Genres", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 5.dp, end = 10.dp))
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            FlowRow(
                modifier = Modifier
                    .width(maxWidth * 2)
                    .horizontalScroll(rememberScrollState()),
                maxItemsInEachRow = (allGenres.count() / 3f).roundToInt(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                allGenres.forEachIndexed { index, genre ->
                    //val isSelected = selectedGenres.contains(genre)
                    FilterChip(
                        label = { Text(text = genre.name) },
                        selected = selectedGenres.contains(genre),
                        onClick = {
                            if (selectedGenres.contains(genre)) {
                                selectedGenres.remove(genre)
                            } else {
                                selectedGenres.add(genre)
                            }
                        },
                        modifier = Modifier
                            .animateContentSize()
                            .padding(horizontal = 3.dp)
                    )
                }
            }
        }

        // Apply Filters button
        Button(onClick = { onFilterApplied(
            TitleListFilter(
                genres = selectedGenres,
                scoreRange = Pair(scoreRange.start.roundToInt(), scoreRange.endInclusive.roundToInt()),
                titleType = null,
                yearsRange = Pair(yearsRange.start.roundToInt(), yearsRange.endInclusive.roundToInt())
            )
        ) }) {
            Text(text = "Apply Filter")
        }
    }
}

