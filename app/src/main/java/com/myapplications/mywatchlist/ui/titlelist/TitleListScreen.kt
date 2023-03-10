package com.myapplications.mywatchlist.ui.titlelist

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
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
    val listState = rememberLazyListState()

    val viewModel = hiltViewModel<TitleListViewModel>()
    val screenTitle by viewModel.screenTitle.collectAsState()
    val titleListUiState by viewModel.titleListState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    val showFilterState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val navigationBarPadding = contentPadding.calculateBottomPadding()

    var showFullFilterBottomSheet by rememberSaveable { mutableStateOf(false) }
    val skipHalfExpanded by remember { mutableStateOf(true) }
    val fullFilterBottomSheetState = rememberSheetState(skipHalfExpanded = skipHalfExpanded)

    var showIndividualFilterBottomSheet by rememberSaveable { mutableStateOf(false) }
    val individualFilterBottomSheetState = rememberSheetState(skipHalfExpanded = false)
    var individualFilterTypeToShow: FilterBottomSheetType? by remember {
        mutableStateOf<FilterBottomSheetType?>(null)
    }

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
                    start = 16.dp,
                    end = 16.dp,
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
                                scope.launch {
//                                    showFilterState.open()
                                    showFullFilterBottomSheet = !showFullFilterBottomSheet
                                }
                            },
                            onIndividualFilterButtonClicked = {
                                individualFilterTypeToShow = it
                                showIndividualFilterBottomSheet = true
                            } ,
                            modifier = Modifier,
                            listState = listState
                        )
                    }
                }
            }
            if (showFullFilterBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showFullFilterBottomSheet = false },
                    sheetState = fullFilterBottomSheetState
                ) {
                    FullFilterBottomSheetContent(
                        filterState = filterState ,
                        defaultScoreRange = TitleListUiFilter().getScoreRange(),
                        defaultYearsRange = TitleListUiFilter().getYearsRange(),
                        allGenres = viewModel.getAllGenres(),
                        onFilterApplied = {
                            viewModel.setFilter(it)
                            scope.launch { showFullFilterBottomSheet = false }
                        },
                        onCloseClicked = {
                            scope.launch { showFullFilterBottomSheet = false }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            if (showIndividualFilterBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showIndividualFilterBottomSheet = false },
                    sheetState = individualFilterBottomSheetState
                ) {
                    individualFilterTypeToShow?.let { bottomSheetTpe ->
                        IndividualBottomSheetContent(
                            filterState = filterState,
                            filterBottomSheetType = bottomSheetTpe,
                            defaultScoreRange = TitleListUiFilter().getScoreRange(),
                            defaultYearsRange = TitleListUiFilter().getYearsRange(),
                            onYearsRangeFilterApplied = {
                                viewModel.setYearsRangeFilter(it)
                                scope.launch { showIndividualFilterBottomSheet = false }
                            },
                            onScoreRangeFilterApplied = {
                                viewModel.setScoreRangeFilter(it)
                                scope.launch { showIndividualFilterBottomSheet = false }
                            },
                            onGenresFilterApplied = {
                                viewModel.setGenresFilter(it)
                                scope.launch { showIndividualFilterBottomSheet = false }
                            },
                            onTitleTypeFilterApplied = {
                                viewModel.setTitleTypeFilter(it)
                                scope.launch { showIndividualFilterBottomSheet = false }
                            },
                            onCloseFilterClicked = {
                                scope.launch { showIndividualFilterBottomSheet = false }
                            },
                            allGenres = viewModel.getAllGenres(),
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }

//    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
//        ModalNavigationDrawer(
//            modifier = modifier,
//            drawerState = showFilterState,
//            drawerContent = {
//                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
//                    BoxWithConstraints {
//                        val defaultFilter = TitleListUiFilter()
//                        /* Need to have the FilterSection invisible when it's closed because during
//                        * transition of 'sliding to the left' it is briefly visible because it
//                        * actually exists on the right side of this screen when closed */
//                        if (showFilterState.isAnimationRunning || showFilterState.isOpen) {
//                            FilterSection(
//                                modifier = Modifier
//                                    .width((maxWidth.value * 0.7).dp)
//                                    .statusBarsPadding(),
//                                filterState = filterState,
//                                defaultScoreRange = defaultFilter.getScoreRange(),
//                                defaultYearsRange = defaultFilter.getYearsRange(),
//                                allGenres = viewModel.getAllGenres(),
//                                onFilterApplied = {
//                                    viewModel.setFilter(it)
//                                    scope.launch {
//                                        showFilterState.close()
//                                    }
//                                },
//                                onCloseClicked = {
//                                    scope.launch {
//                                        showFilterState.close()
//                                    }
//                                }
//                            )
//                        }
//                    }
//                }
//            }
//        ) {
//            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
//
//            }
//        }
//    }

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
    onIndividualFilterButtonClicked: (FilterBottomSheetType) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        FilterButtonsRow(
            filterState = filterState,
            onAllFiltersClicked = onAllFiltersClicked,
            onIndividualFilterButtonClicked = onIndividualFilterButtonClicked
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
            listState = listState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullFilterBottomSheetContent(
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

    Column(modifier = modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center), horizontalArrangement = Arrangement.Center){
                Text(text = stringResource(id = R.string.titlelist_filter_label_all_filters), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 2.dp))
            }
            IconButton(onClick = onCloseClicked, modifier = modifier.padding(start = 30.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(id = R.string.cd_close_filter))
            }
        }
        // TITLE TYPE 
        /* null value for filterState.titleType means this titles list will not have the option to
        be filtered differently based on title type, and so we shouldn't show the filter chips */
        if (filterState.titleType != null) {
            Divider(modifier = Modifier.padding(vertical = 5.dp))
            FilterLabel(label = stringResource(id = R.string.titlelist_filter_label_type))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(
                    label = {
                        Text(
                            text = stringResource(id = R.string.filter_movies),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    },
                    selected = (titleTypeFilterSelected == TitleType.MOVIE),
                    onClick = {
                        if (titleTypeFilterSelected != TitleType.MOVIE) {
                            titleTypeFilterSelected = TitleType.MOVIE
                        }
                    }
                )
                FilterChip(
                    label = { 
                        Text(
                            text = stringResource(id = R.string.filter_tv),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    },
                    selected = (titleTypeFilterSelected == TitleType.TV),
                    onClick = {
                        if (titleTypeFilterSelected != TitleType.TV) {
                            titleTypeFilterSelected = TitleType.TV
                        }
                    }
                )
            }
        }
        Divider(modifier = Modifier.padding(vertical = 5.dp))
        
        // YEARS RANGE
        FilterLabel(label = stringResource(id = R.string.titlelist_filter_label_release_year))
        Spacer(modifier = Modifier.height(5.dp))
        SliderValueBoxRow(
            fromText = "From",  // TODO: Stringresource once design is finalized
            toText = "To",  // TODO: Stringresource once design is finalized
            fromValue = { yearsRange.start.roundToInt().toString() },
            toValue = { yearsRange.endInclusive.roundToInt().toString() }
        )
        RangeSlider(
            value = yearsRange,
            onValueChange = { yearsRange = it },
            valueRange = defaultYearsRange,
            modifier = Modifier.fillMaxWidth()
        )
        Divider(modifier = Modifier.padding(vertical = 5.dp))
        
        // SCORE RANGE
        FilterLabel(label = stringResource(id = R.string.titlelist_filter_label_score))
        Spacer(modifier = Modifier.height(5.dp))
        SliderValueBoxRow(
            fromText = "From",  // TODO: Stringresource once design is finalized
            toText = "To",  // TODO: Stringresource once design is finalized
            fromValue = { scoreRange.start.roundToInt().toString() },
            toValue = { scoreRange.endInclusive.roundToInt().toString() }
        )
        RangeSlider(
            value = scoreRange,
            onValueChange = { scoreRange = it },
            valueRange = defaultScoreRange,
            modifier = Modifier.fillMaxWidth()
        )
        Divider(modifier = Modifier.padding(vertical = 5.dp))
        
        // GENRES
        FilterLabel(label = stringResource(id = R.string.titlelist_filter_label_genres))
        LazyColumn(modifier = Modifier.height(600.dp)) {
            items(allGenres) { genre ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(25))
                        .padding(start = 8.dp, end = 8.dp)
                        .clickable {
                            if (selectedGenres.contains(genre)) {
                                selectedGenres.remove(genre)
                            } else {
                                selectedGenres.add(genre)
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = genre.name)
                    Spacer(modifier = Modifier.weight(1f))
                    Checkbox(checked = selectedGenres.contains(genre), onCheckedChange = {
                        if (it) selectedGenres.add(genre) else selectedGenres.remove(genre)
                    })
                }
            }
        }
        Divider(modifier = Modifier.padding(vertical = 5.dp))
        
        // BUTTONS
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = {
                scoreRange = defaultScoreRange
                yearsRange = defaultYearsRange
                selectedGenres.removeAll(selectedGenres)
                titleTypeFilterSelected = filterState.titleType
            }) {
                Text(text = stringResource(id = R.string.titlelist_filter_button_clear_all), textDecoration = TextDecoration.Underline, modifier = Modifier.padding(bottom = 2.dp))
            }
            Button(onClick = {
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
            }) {
                Text(text = stringResource(id = R.string.titlelist_filter_button_apply), modifier = Modifier.padding(bottom = 2.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun IndividualBottomSheetContent(
    filterState: TitleListUiFilter,
    filterBottomSheetType: FilterBottomSheetType,
    defaultScoreRange: ClosedFloatingPointRange<Float>,
    defaultYearsRange: ClosedFloatingPointRange<Float>,
    onYearsRangeFilterApplied: (Pair<Int, Int>) -> Unit,
    onScoreRangeFilterApplied: (Pair<Int, Int>) -> Unit,
    onGenresFilterApplied: (List<Genre>) -> Unit,
    onTitleTypeFilterApplied: (TitleType) -> Unit,
    onCloseFilterClicked: () -> Unit,
    allGenres: List<Genre>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .fillMaxWidth()
//        .verticalScroll(rememberScrollState())
    ){
        when (filterBottomSheetType) {
            FilterBottomSheetType.YearsRange -> {
                RangeSliderWithLabelsSection(
                    startingRange = filterState.getYearsRange(),
                    defaultRange = defaultYearsRange,
                    onRangeFilterApplied = onYearsRangeFilterApplied,
                    onCloseFilterClicked = onCloseFilterClicked,
                    sectionLabel = stringResource(id = R.string.titlelist_filter_label_release_year),
                    fromText = "From",  // TODO: Stringresource once design is finalized,
                    toText = "To",  // TODO: Stringresource once design is finalized
                )
            }
            FilterBottomSheetType.ScoreRange -> {
                RangeSliderWithLabelsSection(
                    startingRange = filterState.getScoreRange(),
                    defaultRange = defaultScoreRange,
                    onRangeFilterApplied = onScoreRangeFilterApplied,
                    onCloseFilterClicked = onCloseFilterClicked,
                    sectionLabel = stringResource(id = R.string.titlelist_filter_label_score),
                    fromText = "From",  // TODO: Stringresource once design is finalized,
                    toText = "To",  // TODO: Stringresource once design is finalized
                )
            }
            FilterBottomSheetType.Genres -> {
                GenresListSection(
                    filterState = filterState,
                    allGenres = allGenres,
                    sectionLabel = stringResource(id = R.string.titlelist_filter_label_genres),
                    onGenresFilterApplied = onGenresFilterApplied,
                    onCloseFilterClicked = onCloseFilterClicked
                )
            }
            FilterBottomSheetType.TitleType -> {
                TitleTypeSection(
                    filterState = filterState,
                    sectionLabel = stringResource(id = R.string.titlelist_filter_label_type),
                    onTitleTypeFilterApplied = onTitleTypeFilterApplied,
                    onCloseFilterClicked = onCloseFilterClicked
                )
            }
        }
    }
}

@Composable
fun RangeSliderWithLabelsSection(
    startingRange: ClosedFloatingPointRange<Float>,
    defaultRange: ClosedFloatingPointRange<Float>,
    onRangeFilterApplied: (Pair<Int, Int>) -> Unit,
    onCloseFilterClicked: () -> Unit,
    sectionLabel: String,
    fromText: String,
    toText: String,
    modifier: Modifier = Modifier
) {
    var range by remember {
        mutableStateOf(startingRange)
    }
    Column(modifier = modifier) {
        FilterLabel(label = sectionLabel, onTrailingIconClicked = onCloseFilterClicked)
        Spacer(modifier = Modifier.height(5.dp))
        SliderValueBoxRow(
            fromText = fromText,
            toText = toText,
            fromValue = { range.start.roundToInt().toString() },
            toValue = { range.endInclusive.roundToInt().toString() }
        )
        RangeSlider(
            value = range,
            onValueChange = { range = it },
            valueRange = defaultRange,
            modifier = Modifier.fillMaxWidth()
        )
        ResetAllAndApplyFilterButtonsRow(
            onResetFilterClicked = { range = defaultRange },
            onFilterAppliedClicked = {
                onRangeFilterApplied(Pair(range.start.roundToInt(), range.endInclusive.roundToInt()))
            }
        )
    }
}

@Composable
fun GenresListSection(
    filterState: TitleListUiFilter,
    allGenres: List<Genre>,
    sectionLabel: String,
    onGenresFilterApplied: (List<Genre>) -> Unit,
    onCloseFilterClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedGenres = remember {
        mutableStateListOf<Genre>().apply {
            addAll(filterState.genres)
        }
    }
    Column(modifier = modifier) {
        LazyColumn() {
            item {
                FilterLabel(label = sectionLabel, onTrailingIconClicked = onCloseFilterClicked)
            }
            items(allGenres) { genre ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(25))
                        .padding(start = 8.dp, end = 8.dp)
                        .clickable {
                            if (selectedGenres.contains(genre)) {
                                selectedGenres.remove(genre)
                            } else {
                                selectedGenres.add(genre)
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = genre.name)
                    Spacer(modifier = Modifier.weight(1f))
                    Checkbox(checked = selectedGenres.contains(genre), onCheckedChange = {
                        if (it) selectedGenres.add(genre) else selectedGenres.remove(genre)
                    })
                }
            }
            item {
                ResetAllAndApplyFilterButtonsRow(
                    onResetFilterClicked = { selectedGenres.removeAll(selectedGenres) },
                    onFilterAppliedClicked = { onGenresFilterApplied(selectedGenres) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleTypeSection(
    filterState: TitleListUiFilter,
    sectionLabel: String,
    onTitleTypeFilterApplied: (TitleType) -> Unit,
    onCloseFilterClicked: () -> Unit,
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
    Column(modifier = modifier) {
        FilterLabel(label = sectionLabel, onTrailingIconClicked = onCloseFilterClicked)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
             FilterChip(
                label = {
                    Text(
                        text = stringResource(id = R.string.filter_movies),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                },
                selected = (titleTypeFilterSelected == TitleType.MOVIE),
                onClick = {
                    if (titleTypeFilterSelected != TitleType.MOVIE) {
                        titleTypeFilterSelected = TitleType.MOVIE
                    }
                }
            )
            FilterChip(
                label = {
                    Text(
                        text = stringResource(id = R.string.filter_tv),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                },
                selected = (titleTypeFilterSelected == TitleType.TV),
                onClick = {
                    if (titleTypeFilterSelected != TitleType.TV) {
                        titleTypeFilterSelected = TitleType.TV
                    }
                }
            )
        }
        ResetAllAndApplyFilterButtonsRow(
            onResetFilterClicked = { titleTypeFilterSelected = filterState.titleType },
            onFilterAppliedClicked = {
                titleTypeFilterSelected?.let{
                    onTitleTypeFilterApplied(it)
                }
            }
        )
    }
}
@Composable
fun ResetAllAndApplyFilterButtonsRow(
    onResetFilterClicked: () -> Unit,
    onFilterAppliedClicked: () -> Unit,
    modifier: Modifier = Modifier,
    resetButtonText: String = stringResource(id = R.string.titlelist_filter_button_clear),
    applyFilterButtonText: String = stringResource(id = R.string.titlelist_filter_button_apply)
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onResetFilterClicked) {
            Text(
                text = resetButtonText,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Button(onClick = onFilterAppliedClicked) {
            Text(text = applyFilterButtonText, modifier = Modifier.padding(bottom = 2.dp))
        }
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
    onIndividualFilterButtonClicked: (FilterBottomSheetType) -> Unit,
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

    Row(modifier = modifier.horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = onAllFiltersClicked, modifier = modifier.padding(end = 5.dp)) {
            Text(text = stringResource(id = R.string.titlelist_filter_label_all_filters))
        }

        if (filterState.titleType != null) {
            OutlinedButton(
                onClick = { onIndividualFilterButtonClicked(FilterBottomSheetType.TitleType) },
                modifier = modifier.padding(end = 5.dp)
            ) {
                Text(text = "Title Type")
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = stringResource(id = R.string.cd_expand_more)
                )
            }
        }

        OutlinedButton(
            onClick = { onIndividualFilterButtonClicked(FilterBottomSheetType.YearsRange) },
            modifier = modifier.padding(end = 5.dp)
        ) {
            Text(text = "Year")
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = stringResource(id = R.string.cd_expand_more)
            )
        }

        OutlinedButton(
            onClick = { onIndividualFilterButtonClicked(FilterBottomSheetType.ScoreRange) },
            modifier = modifier.padding(end = 5.dp)
        ) {
            Text(text = "Score")
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = stringResource(id = R.string.cd_expand_more)
            )
        }

        OutlinedButton(
            onClick = { onIndividualFilterButtonClicked(FilterBottomSheetType.Genres) },
            modifier = modifier.padding(end = 5.dp)
        ) {
            Text(text = "Genres")
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = stringResource(id = R.string.cd_expand_more)
            )
        }


        /* TODO: commented out quick TV/Movie filter Chip group because currently displayed lists
        *   are only separate movies OR tv lists. Need to review this and delete / remake once
        *   Discover / Custom Filter screen is implemented.  */
//        Spacer(modifier = Modifier.width(15.dp))
//        Spacer( modifier = Modifier
//            .height(ButtonDefaults.MinHeight)
//            .padding(vertical = 2.dp)
//            .width(0.5.dp)
//            .background(MaterialTheme.colorScheme.onSurfaceVariant)
//        )
//        Spacer(modifier = Modifier.width(10.dp))
//        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
//            FilterChipGroup(
//                onFilterSelected = { titleTypeFilter ->
//                    titleTypeFilterSelected = when (titleTypeFilter) {
//                        TitleTypeFilter.All -> null
//                        TitleTypeFilter.Movies -> TitleType.MOVIE
//                        TitleTypeFilter.TV -> TitleType.TV
//                    }
//                    onTypeFilterSelected(titleTypeFilterSelected)
//                },
//                horizontalArrangement = Arrangement.spacedBy(10.dp),
//                modifier = Modifier,
//                filter = when (titleTypeFilterSelected) {
//                    TitleType.MOVIE -> TitleTypeFilter.Movies
//                    TitleType.TV -> TitleTypeFilter.TV
//                    null -> TitleTypeFilter.All
//                },
//            )
//        }
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

            /* TODO: Commenting out because currently lists shall not be filtered by type because
            *   we are displaying only movies or tv lists separately. This should be remade later
            *   once Discover / Custom Filter screen is implemented */
//            // Type Filter
//            FilterLabel(label = stringResource(id = R.string.titlelist_filter_label_type))
//            FilterChipGroup(
//                onFilterSelected = { titleTypeFilter ->
//                    titleTypeFilterSelected = when (titleTypeFilter) {
//                        TitleTypeFilter.All -> null
//                        TitleTypeFilter.Movies -> TitleType.MOVIE
//                        TitleTypeFilter.TV -> TitleType.TV
//                    }
//                },
//                modifier = Modifier,
//                filter = when (titleTypeFilterSelected) {
//                    TitleType.MOVIE -> TitleTypeFilter.Movies
//                    TitleType.TV -> TitleTypeFilter.TV
//                    null -> TitleTypeFilter.All
//                },
//            )
//            FilterSectionDivider()

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
                    Text(text = stringResource(id = R.string.titlelist_filter_button_clear_all))
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

/**
 * Simple FilterLabel that just shows the label.
 */
@Composable
fun FilterLabel(
    label: String,
    modifier: Modifier = Modifier,
    labelStyle: TextStyle = MaterialTheme.typography.titleLarge,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingValues),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = labelStyle)
    }
}

/**
 * FilterLabel that shows the label and an icon at the end of the row.
 */
@Composable
fun FilterLabel(
    label: String,
    modifier: Modifier = Modifier,
    labelStyle: TextStyle = MaterialTheme.typography.titleLarge,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    trailingIconVector: ImageVector = Icons.Default.Close,
    iconContentDescription: String = stringResource(id = R.string.cd_close_filter),
    onTrailingIconClicked: () -> Unit
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingValues),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = labelStyle, modifier = Modifier.padding(bottom = 2.dp))
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onTrailingIconClicked) {
            Icon(imageVector = trailingIconVector, contentDescription = iconContentDescription)
        }
    }
}

@Composable
fun FilterLabel(
    label: String,
    rangeText: () -> String,
    modifier: Modifier = Modifier,
    labelStyle: TextStyle = MaterialTheme.typography.titleLarge,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingValues),
        verticalAlignment = Alignment.CenterVertically
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
fun SliderValueBoxRow(
    fromText: String,
    toText: String,
    fromValue: () -> String,
    toValue: () -> String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .widthIn(min = 60.dp)
                .border(
                    width = Dp.Hairline,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(vertical = 5.dp, horizontal = 8.dp)
        ) {
            Text(text = fromText)
            Text(text = fromValue())
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .widthIn(min = 60.dp)
                .border(
                    width = Dp.Hairline,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(vertical = 5.dp, horizontal = 8.dp)

        ) {
            Text(text = toText)
            Text(text = toValue())
        }
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

enum class FilterBottomSheetType {
    YearsRange,
    ScoreRange,
    Genres,
    TitleType
}