package com.myapplications.mywatchlist.ui.titlelist

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.core.util.extensions.isScrollingUp
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.ui.components.*
import com.myapplications.mywatchlist.ui.entities.TitleListFilter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleListScreen(
    placeholderPoster: Painter,
    onTitleClicked: (TitleItemFull) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {

    val viewModel = hiltViewModel<TitleListViewModel>()
    val titleListUiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    val listState = rememberLazyListState()
    var showFilter by remember { mutableStateOf(false) }

    val navigationBarPadding = contentPadding.calculateBottomPadding()
    val topAppBarPadding = contentPadding.calculateTopPadding()
    
    Button(onClick = { /*TODO*/ }) {
        Text(text = "LOLLOLOLOLOLOLOL")
    }
    
    ModalNavigationDrawer(drawerContent = { /*TODO*/ }) {
        
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FilterFAB(
                isFabVisible = listState.isScrollingUp(),
                onFabClicked = { showFilter = !showFilter },
                modifier = Modifier.padding(bottom = navigationBarPadding, top = topAppBarPadding)
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = navigationBarPadding)
        ) {
            Crossfade(
                targetState = titleListUiState,
                modifier = Modifier.padding(top = topAppBarPadding)
            ) { uiState ->
                when (uiState) {
                    TitleListUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
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
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            //#region FILTERS SECTION. WILL BE UPDATED WHEN DESIGNED FINISHES THE UI/UX
//                            val defaultFilter = TitleListFilter()
//                            AnimatedVisibility(
//                                visible = showFilter,
//                                enter = expandVertically(
//                                    expandFrom = Alignment.Top,
//                                    animationSpec = tween(
//                                        durationMillis = 300,
//                                        easing = FastOutSlowInEasing
//                                    )
//                                ),
//                                exit = shrinkVertically(
//                                    shrinkTowards = Alignment.Top,
//                                    animationSpec = tween(
//                                        durationMillis = 300,
//                                        easing = LinearOutSlowInEasing
//                                    )
//                                )
//                            ) {
//                                FilterSection(
//                                    filter = filterState,
//                                    defaultScoreRange = defaultFilter.scoreRange.first.toFloat()..defaultFilter.scoreRange.second.toFloat(),
//                                    defaultYearsRange = defaultFilter.yearsRange.first.toFloat()..defaultFilter.yearsRange.second.toFloat(),
//                                    allGenres = viewModel.getAllGenres(),
//                                    onFilterApplied = {
//                                        viewModel.setFilter(it)
//                                        showFilter = false
//                                    },
//                                    onCancelClicked = { showFilter = false }
//                                )
//                            }
//                            //#endregion

                            //#region TITLE ITEMS LIST
                            TitleItemsList(
                                titleItemsFull = uiState.titleItems,
                                placeholderImage = placeholderPoster,
                                onWatchlistClicked = viewModel::onWatchlistClicked,
                                onTitleClicked = onTitleClicked,
                                contentPadding = PaddingValues(vertical = 10.dp),
                                state = listState
                            )
                            //#endregion
                        }
                    }
                }
            }
            if (showFilter) {
                Box(
                    modifier = Modifier
                        .size(maxWidth / 3 * 2, height = maxHeight)
                        .offset(x = maxWidth / 3)
                ) {
                    FilterSideSheet()
                }
            }
        }
    }

//    Box(
//        modifier = modifier.fillMaxSize()
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 16.dp)
//        ) {
//            Crossfade(targetState = titleListUiState) { uiState ->
//                when (uiState) {
//                    TitleListUiState.Loading -> {
//                        Box(
//                            modifier = Modifier.fillMaxSize(),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            LoadingCircle()
//                        }
//                    }
//                    is TitleListUiState.Error -> {
//                        Column(
//                            modifier = Modifier.fillMaxSize(),
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.Center
//                        ) {
//                            when (uiState.error) {
//                                TitleListError.NO_INTERNET -> {
//                                    ErrorText(
//                                        errorMessage =
//                                        stringResource(id = R.string.error_no_internet_connection),
//                                        onButtonRetryClick = { viewModel.retryGetData() }
//                                    )
//                                }
//                                TitleListError.FAILED_API_REQUEST,
//                                TitleListError.UNKNOWN -> {
//                                    ErrorText(
//                                        errorMessage =
//                                        stringResource(id = R.string.error_something_went_wrong),
//                                        onButtonRetryClick = { viewModel.retryGetData() }
//                                    )
//                                }
//                                TitleListError.NO_TITLES -> {
//                                    ErrorText(
//                                        errorMessage = stringResource(id = R.string.titlelist_no_data)
//                                    )
//                                }
//                            }
//                        }
//                    }
//                    is TitleListUiState.Ready -> {
//                        Column(
//                            modifier = Modifier.fillMaxSize()
//                        ) {
//                            //#region FILTERS SECTION. WILL BE UPDATED WHEN DESIGNED FINISHES THE UI/UX
//                            val defaultFilter = TitleListFilter()
//                            AnimatedVisibility(
//                                visible = showFilter,
//                                enter = expandVertically(
//                                    expandFrom = Alignment.Top,
//                                    animationSpec = tween(
//                                        durationMillis = 300,
//                                        easing = FastOutSlowInEasing
//                                    )
//                                ),
//                                exit = shrinkVertically(
//                                    shrinkTowards = Alignment.Top,
//                                    animationSpec = tween(
//                                        durationMillis = 300,
//                                        easing = LinearOutSlowInEasing
//                                    )
//                                )
//                            ) {
//                                FilterSection(
//                                    filter = filterState,
//                                    defaultScoreRange = defaultFilter.scoreRange.first.toFloat()..defaultFilter.scoreRange.second.toFloat(),
//                                    defaultYearsRange = defaultFilter.yearsRange.first.toFloat()..defaultFilter.yearsRange.second.toFloat(),
//                                    allGenres = viewModel.getAllGenres(),
//                                    onFilterApplied = {
//                                        viewModel.setFilter(it)
//                                        showFilter = false
//                                    },
//                                    onCancelClicked = { showFilter = false }
//                                )
//                            }
//                            //#endregion
//
//                            //#region TITLE ITEMS LIST
//                            TitleItemsList(
//                                titleItemsFull = uiState.titleItems,
//                                placeholderImage = placeholderPoster,
//                                onWatchlistClicked = viewModel::onWatchlistClicked,
//                                onTitleClicked = onTitleClicked,
//                                contentPadding = PaddingValues(vertical = 10.dp),
//                                state = listState
//                            )
//                            //#endregion
//                        }
//                    }
//                }
//            }
//        }
//        FilterFAB(
//            isFabVisible = listState.isScrollingUp(),
//            onFabClicked = { showFilter = !showFilter },
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(bottom = 20.dp, end = 20.dp)
//        )
//    }
}

@Composable
fun FilterSideSheet(
    modifier: Modifier = Modifier
) {
    //#region CONTENT
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            .padding(horizontal = 5.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        repeat(16) {
            // Score Slider
            FilterLabel(label = stringResource(id = R.string.titlelist_filter_label_score), rangeText = { "Score 0 - 10" })
            FilterSectionDivider()

            // Years Slider
            FilterLabel(label = stringResource(id = R.string.titlelist_filter_label_release_year), rangeText = { "Years 1960 - 2023 " })
            FilterSectionDivider()
            Spacer(modifier = Modifier.height(10.dp))
        }

    }
    //#endregion
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSection(
    filter: TitleListFilter,
    defaultScoreRange: ClosedFloatingPointRange<Float>,
    defaultYearsRange: ClosedFloatingPointRange<Float>,
    allGenres: List<Genre>,
    onFilterApplied: (TitleListFilter) -> Unit,
    onCancelClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var titleTypeFilterSelected by remember { mutableStateOf(filter.titleType) }
    var scoreRange by remember {
        mutableStateOf(filter.scoreRange.first.toFloat()..filter.scoreRange.second.toFloat())
    }
    var yearsRange by remember {
        mutableStateOf(filter.yearsRange.first.toFloat()..filter.yearsRange.second.toFloat())
    }
    val selectedGenres = remember {
        mutableStateListOf<Genre>().apply {
            addAll(filter.genres)
        }
    }
    val scoreRangeText by remember {
        derivedStateOf {
            "${scoreRange.start.roundToInt()} - ${scoreRange.endInclusive.roundToInt()}"
        }
    }
    val yearsRangeText by remember {
        derivedStateOf {
            "${yearsRange.start.roundToInt()} - ${yearsRange.endInclusive.roundToInt()}"
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {

        // Type Filter
        FilterLabel(label = stringResource(id = R.string.titlelist_filter_label_type))
        FilterChipGroup(
            onFilterSelected = { titleTypeFilter ->
                titleTypeFilterSelected = when (titleTypeFilter) {
                    TitleTypeFilter.All -> null
                    TitleTypeFilter.Movies -> TitleType.MOVIE
                    TitleTypeFilter.TV -> TitleType.TV
                }
            },
            modifier = Modifier,
            filter = when (titleTypeFilterSelected) {
                TitleType.MOVIE -> TitleTypeFilter.Movies
                TitleType.TV -> TitleTypeFilter.TV
                null -> TitleTypeFilter.All
            },
        )
        FilterSectionDivider()

        // Score Slider
        FilterLabel(
            label = stringResource(id = R.string.titlelist_filter_label_score),
            rangeText = { scoreRangeText }
        )
        RangeSlider(
            value = scoreRange,
            onValueChange = { scoreRange = it },
            valueRange = defaultScoreRange,
            modifier = Modifier.fillMaxWidth()
        )
        FilterSectionDivider()

        // Years Slider
        FilterLabel(
            label = stringResource(id = R.string.titlelist_filter_label_release_year),
            rangeText = { yearsRangeText }
        )
        RangeSlider(
            value = yearsRange,
            onValueChange = { yearsRange = it },
            valueRange = defaultYearsRange,
            modifier = Modifier.fillMaxWidth()
        )
        FilterSectionDivider()

        // Genres Flow
        FilterLabel(label = stringResource(id = R.string.titlelist_filter_label_genres))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            FlowRow(
                modifier = Modifier
                    .width(maxWidth * 2)
                    .horizontalScroll(rememberScrollState()),
                maxItemsInEachRow = (allGenres.count() / 3f).roundToInt(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                allGenres.forEach { genre ->
                    FilterChip(
                        label = { Text(
                            text = genre.name,
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) },
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
                            .padding(horizontal = 3.dp),
                    )
                }
            }
        }

        FilterSectionDivider()

        // Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            OutlinedButton(
                onClick = onCancelClicked,
                modifier = Modifier.widthIn(min = 60.dp)
            ) {
                Text(text = stringResource(id = R.string.titlelist_filter_button_cancel))
            }
            FilledTonalButton(
                onClick = {
                    scoreRange = defaultScoreRange
                    yearsRange = defaultYearsRange
                    selectedGenres.removeAll(selectedGenres)
                    titleTypeFilterSelected = null
                },
                modifier = Modifier.widthIn(min = 60.dp)
            ) {
                Text(text = stringResource(id = R.string.titlelist_filter_button_clear))
            }
            Button(
                onClick = {
                    onFilterApplied(
                        TitleListFilter(
                            genres = selectedGenres,
                            scoreRange = Pair(
                                scoreRange.start.roundToInt(),
                                scoreRange.endInclusive.roundToInt()
                            ),
                            titleType = titleTypeFilterSelected,
                            yearsRange = Pair(
                                yearsRange.start.roundToInt(),
                                yearsRange.endInclusive.roundToInt()
                            )
                        )
                    )
                },
                modifier = Modifier.widthIn(min = 60.dp)
            ) {
                Text(text = stringResource(id = R.string.titlelist_filter_button_apply))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun FilterLabel(
    label: String,
    modifier: Modifier = Modifier,
    labelStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingValues),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(text = label, style = labelStyle)
    }
}

@Composable
fun FilterLabel(
    label: String,
    rangeText: () -> String,
    modifier: Modifier = Modifier,
    labelStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingValues),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(text = label, style = labelStyle)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = rangeText(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(0.dp)
        )
    }
}

@Composable
fun FilterSectionDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    thickness: Dp = Dp.Hairline,
    paddingValues: PaddingValues = PaddingValues(vertical = 5.dp)
) {
    Divider(modifier = modifier.padding(paddingValues), thickness = thickness, color = color)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FilterFAB(
    isFabVisible: Boolean,
    onFabClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isFabVisible,
        modifier = modifier,
        enter = scaleIn(
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            transformOrigin = TransformOrigin(1f, 1f)
        ),
        exit = scaleOut(
            animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
            transformOrigin = TransformOrigin(1f, 1f)
        )
    ) {
        SmallFloatingActionButton(onClick = onFabClicked) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = stringResource(id = R.string.cd_filter_icon)
            )
        }
    }
}

