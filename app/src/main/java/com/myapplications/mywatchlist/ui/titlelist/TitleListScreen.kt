package com.myapplications.mywatchlist.ui.titlelist

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.myapplications.mywatchlist.R
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.ui.MyTopAppBar
import com.myapplications.mywatchlist.ui.components.*
import com.myapplications.mywatchlist.ui.entities.TitleListType
import com.myapplications.mywatchlist.ui.entities.TitleListUiFilter
import com.myapplications.mywatchlist.ui.entities.UiError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val TAG = "TITLE_LIST_SCREEN"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleListScreen(
    placeholderPoster: Painter,
    onTitleClicked: (TitleItemFull) -> Unit,
    onNavigateUp: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val viewModel = hiltViewModel<TitleListViewModel>()
    val screenTitle by viewModel.screenTitle.collectAsState()
    val titleListUiState by viewModel.titleListState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    val showFilterState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val navigationBarPadding = contentPadding.calculateBottomPadding()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            modifier = modifier,
            drawerState = showFilterState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    BoxWithConstraints {
                        val defaultFilter = TitleListUiFilter()
                        /* Need to have the FilterSection invisible when it's closed because during
                        * transition of 'sliding to the left' it is briefly visible because it
                        * actually exists on the right side of this screen when closed */
                        if (showFilterState.isAnimationRunning || showFilterState.isOpen) {
                            FilterSection(
                                modifier = Modifier
                                    .width((maxWidth.value * 0.7).dp)
                                    .statusBarsPadding(),
                                filterState = filterState,
                                defaultScoreRange = defaultFilter.getScoreRange(),
                                defaultYearsRange = defaultFilter.getYearsRange(),
                                allGenres = viewModel.getAllGenres(),
                                onFilterApplied = {
                                    viewModel.setFilter(it)
                                    scope.launch {
                                        showFilterState.close()
                                    }
                                },
                                onCloseClicked = {
                                    scope.launch {
                                        showFilterState.close()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    topBar = {
                        MyTopAppBar(
                            title = getScreenTitle(screenTitle), showUpButton = true, onNavigateUp = onNavigateUp
                        )
                    }
                ) { paddingValues ->
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 16.dp, end = 16.dp,
                                bottom = navigationBarPadding,
                                top = paddingValues.calculateTopPadding()
                            )
                    ) {
                        Crossfade(targetState = titleListUiState) { titleListUiState ->
                            when (titleListUiState) {
                                is TitleListUiState.Error -> {
                                    TitleListCenteredErrorMessage(
                                        error = (titleListUiState).error,
                                        onButtonRetryClick = { viewModel.retryGetData() }
                                    )
                                }
                                TitleListUiState.Loading -> {
                                    FullScreenLoadingCircle()
                                }
                                is TitleListUiState.Ready -> {
                                    val titles = (titleListUiState).titles.collectAsLazyPagingItems()
                                    TitleListScreenContentNew(
                                        titlesList = titles,
                                        filterState = filterState,
                                        error = { viewModel.getErrorFromResultThrowable(it) },
                                        placeholderPoster = placeholderPoster,
                                        onRetryEntireListClick = { viewModel.retryGetData() },
                                        onWatchlistClicked = viewModel::onWatchlistClicked,
                                        onTitleClicked = onTitleClicked,
                                        onAllFiltersClicked = {
                                            scope.launch { showFilterState.open() }
                                        },
                                        onTitleTypeFilterSelected = {
                                            viewModel.setFilter(filterState.copy(titleType = it))
                                            titles.refresh()
                                        },
                                        modifier = Modifier,
                                        listState = rememberLazyListState(),
                                        coroutineScope = scope
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TitleListScreenContentNew(
    titlesList: LazyPagingItems<TitleItemFull>,
    filterState: TitleListUiFilter,
    error: (Throwable) -> UiError,
    placeholderPoster: Painter,
    onRetryEntireListClick: () -> Unit,
    onWatchlistClicked: (TitleItemFull) -> Unit,
    onTitleClicked: (TitleItemFull) -> Unit,
    onAllFiltersClicked: () -> Unit,
    onTitleTypeFilterSelected: (TitleType?) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        FilterButtonsRow(
            filterState = filterState,
            onAllFiltersClicked = onAllFiltersClicked,
            onTypeFilterSelected = onTitleTypeFilterSelected
        )

        TitleItemsListPaginated(
            titles = titlesList,
            error = error,
            errorComposable = {
                TitleListCenteredErrorMessage(
                    error = it,
                    onButtonRetryClick = onRetryEntireListClick
                )
            },
            onWatchlistClicked = onWatchlistClicked,
            onTitleClicked = { onTitleClicked(it) },
            placeHolderPoster = placeholderPoster,
            listState = listState,
            scope = coroutineScope
        )
    }
}

@Composable
fun FullScreenLoadingCircle(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LoadingCircle()
    }
}

@Composable
fun TitleListCenteredErrorMessage(
    error: UiError,
    onButtonRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (error) {
            TitleListError.NO_INTERNET -> {
                ErrorText(
                    errorMessage =
                    stringResource(id = R.string.error_no_internet_connection),
                    onButtonRetryClick = onButtonRetryClick
                )
            }
            TitleListError.FAILED_API_REQUEST,
            TitleListError.UNKNOWN -> {
                ErrorText(
                    errorMessage =
                    stringResource(id = R.string.error_something_went_wrong),
                    onButtonRetryClick = onButtonRetryClick
                )
            }
            TitleListError.NO_TITLES -> {
                ErrorText(
                    errorMessage = stringResource(id = R.string.titlelist_no_data)
                )
            }
            else -> ErrorText(
                errorMessage =
                stringResource(id = R.string.error_something_went_wrong),
                onButtonRetryClick = onButtonRetryClick
            )
        }
    }
}

@Composable
fun FilterButtonsRow(
    filterState: TitleListUiFilter,
    onAllFiltersClicked: () -> Unit,
    onTypeFilterSelected: (TitleType?) -> Unit,
    modifier: Modifier = Modifier
) {
    var titleTypeFilterSelected by remember { mutableStateOf(filterState.titleType) }
    /* Changing the rememberer TitleType filter variable each time the filterState's value changes.
    This is required when the TitleType is changed in the filter menu above the list and
    FilterSection needs to know that so that when it's opened it can properly show the selected
    TitleType filer. */
    LaunchedEffect(key1 = filterState.titleType) {
        titleTypeFilterSelected = filterState.titleType
    }

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = onAllFiltersClicked) {
            Text(text = stringResource(id = R.string.titlelist_filter_label_all_filters))
        }
        Spacer(modifier = Modifier.width(15.dp))
        Spacer( modifier = Modifier
            .height(ButtonDefaults.MinHeight)
            .padding(vertical = 2.dp)
            .width(0.5.dp)
            .background(MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            FilterChipGroup(
                onFilterSelected = { titleTypeFilter ->
                    titleTypeFilterSelected = when (titleTypeFilter) {
                        TitleTypeFilter.All -> null
                        TitleTypeFilter.Movies -> TitleType.MOVIE
                        TitleTypeFilter.TV -> TitleType.TV
                    }
                    onTypeFilterSelected(titleTypeFilterSelected)
                },
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier,
                filter = when (titleTypeFilterSelected) {
                    TitleType.MOVIE -> TitleTypeFilter.Movies
                    TitleType.TV -> TitleTypeFilter.TV
                    null -> TitleTypeFilter.All
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSection(
    filterState: TitleListUiFilter,
    defaultScoreRange: ClosedFloatingPointRange<Float>,
    defaultYearsRange: ClosedFloatingPointRange<Float>,
    allGenres: List<Genre>,
    onFilterApplied: (TitleListUiFilter) -> Unit,
    onCloseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var titleTypeFilterSelected by remember { mutableStateOf(filterState.titleType) }
    /* Changing the rememberer TitleType filter variable each time the filterState's value changes.
    This is required when the TitleType is changed in the filter menu above the list and
    FilterSection needs to know that so that when it's opened it can properly show the selected
    TitleType filer. */
    LaunchedEffect(key1 = filterState.titleType) {
        titleTypeFilterSelected = filterState.titleType
    }
    var scoreRange by remember {
        mutableStateOf(filterState.getScoreRange())
    }
    var yearsRange by remember {
        mutableStateOf(filterState.getYearsRange())
    }
    val selectedGenres = remember {
        mutableStateListOf<Genre>().apply {
            addAll(filterState.genres)
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.titlelist_filter_label_all_filters),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 2.dp) //Adjustment due to low font height
                )
            }
            IconButton(onClick = onCloseClicked, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.cd_close_filter)
                )
            }
        }
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
        ) {
            FilterSectionDivider(paddingValues = PaddingValues(bottom = 5.dp))

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
                            label = {
                                Text(
                                    text = genre.name,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            },
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
                TextButton(
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
                        val mFilter = TitleListUiFilter(
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
                        onFilterApplied(mFilter)
                    },
                    modifier = Modifier.widthIn(min = 60.dp)
                ) {
                    Text(text = stringResource(id = R.string.titlelist_filter_button_apply))
                }
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

@Composable
fun getScreenTitle(titleListType: TitleListType?): String {
    return when (titleListType) {
        TitleListType.DiscoverMovies,
        TitleListType.DiscoverTV ->
            stringResource(id = R.string.titlelist_screen_title_discover)
        TitleListType.PopularMovies ->
            stringResource(id = R.string.titlelist_screen_title_popular_movies)
        TitleListType.PopularTV ->
            stringResource(id = R.string.titlelist_screen_title_popular_tv)
        TitleListType.TopRatedMovies ->
            stringResource(id = R.string.titlelist_screen_title_toprated_movies)
        TitleListType.TopRatedTV ->
            stringResource(id = R.string.titlelist_screen_title_toprated_tv)
        TitleListType.UpcomingMovies ->
            stringResource(id = R.string.titlelist_screen_title_upcoming_movies)
        null -> ""
    }
}