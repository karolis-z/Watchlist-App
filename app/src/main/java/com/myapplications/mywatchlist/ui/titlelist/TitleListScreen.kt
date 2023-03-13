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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
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

private val AppBarHeight = 64.dp
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

    val navigationBarPadding = contentPadding.calculateBottomPadding()

    var showFullFilterBottomSheet by rememberSaveable { mutableStateOf(false) }
    val skipHalfExpanded by remember { mutableStateOf(true) }
    val fullFilterBottomSheetState = rememberSheetState(
        skipHalfExpanded = skipHalfExpanded,
        /* For some reason when bottomsheet is closed by dragging NOT by the handle, onDismissRequest
        * is not called and showFullFilterBottomSheet doesn't get changed to false. Using this
        * confirmValueChange callback to set showFullFilterBottomSheet to false in such cases so
        * that the bottom sheet doesn't freeze when 'incorrectly' closed */
        confirmValueChange = {
            if (it == SheetValue.Hidden && showFullFilterBottomSheet) {
                showFullFilterBottomSheet = false 
                false
            } else {
                true
            }
        }
    )

    var showIndividualFilterBottomSheet by rememberSaveable { mutableStateOf(false) }
    val individualFilterBottomSheetState = rememberSheetState(
        skipHalfExpanded = true,
        /* For some reason when bottomsheet is closed by dragging NOT by the handle, onDismissRequest
        * is not called and showIndividualFilterBottomSheet doesn't get changed to false. Using this
        * confirmValueChange callback to set showIndividualFilterBottomSheet to false in such cases
        * so that the bottom sheet doesn't freeze when 'incorrectly' closed */
        confirmValueChange = {
            if (it == SheetValue.Hidden && showIndividualFilterBottomSheet) {
                showIndividualFilterBottomSheet = false
                false
            } else {
                true
            }
        }
    )
    var individualFilterTypeToShow: FilterBottomSheetType? by remember { mutableStateOf(null) }

    fun closeIndividualBottomSheet() {
        scope.launch { individualFilterBottomSheetState.hide() }
            .invokeOnCompletion {
                if (!individualFilterBottomSheetState.isVisible) {
                    showIndividualFilterBottomSheet = false
                }
            }
    }

    Scaffold(
        topBar = {
            MyTopAppBar(
                title = getScreenTitle(screenTitle), showUpButton = true, onNavigateUp = onNavigateUp
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = modifier
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
                            onAllFiltersClicked = { showFullFilterBottomSheet = true },
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
                    sheetState = fullFilterBottomSheetState,
                    shape = RectangleShape,
                    dragHandle = null
                ) {
                    Column(modifier = Modifier) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                                .height(AppBarHeight)
                        ) {
                            IconButton(
                                onClick = {
                                    scope.launch { fullFilterBottomSheetState.hide() }
                                        .invokeOnCompletion {
                                            if (!fullFilterBottomSheetState.isVisible) {
                                                showFullFilterBottomSheet = false
                                            }
                                        }
                                },
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = stringResource(id = R.string.cd_close_filter),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = stringResource(id = R.string.titlelist_filter_label_all_filters),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .padding(bottom = 2.dp)
                                    .align(Alignment.Center)
                            )
                        }
                        FullFilterBottomSheetContent(
                            filterState = filterState,
                            defaultScoreRange = filterState.getDefaultScoreRange(),
                            defaultYearsRange = filterState.getDefaultYearsRange(),
                            allGenres = viewModel.getAllGenres(),
                            onFilterApplied = {
                                viewModel.setFilter(it)
                                scope.launch { fullFilterBottomSheetState.hide() }
                                    .invokeOnCompletion {
                                        if (!fullFilterBottomSheetState.isVisible) {
                                            showFullFilterBottomSheet = false
                                        }
                                    }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }
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
                                closeIndividualBottomSheet()
                            },
                            onScoreRangeFilterApplied = {
                                viewModel.setScoreRangeFilter(it)
                                closeIndividualBottomSheet()
                            },
                            onGenresFilterApplied = {
                                viewModel.setGenresFilter(it)
                                closeIndividualBottomSheet()
                            },
                            onTitleTypeFilterApplied = {
                                viewModel.setTitleTypeFilter(it)
                                closeIndividualBottomSheet()
                            },
                            onCloseFilterClicked = { closeIndividualBottomSheet() },
                            allGenres = viewModel.getAllGenres(),
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

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
        Spacer(modifier = Modifier.height(8.dp))
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

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f)
        ) {
            // LABEL AND CLOSE BUTTON
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .wrapContentHeight()
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .align(Alignment.Center),
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    Text(
//                        text = stringResource(id = R.string.titlelist_filter_label_all_filters),
//                        style = MaterialTheme.typography.titleLarge,
//                        modifier = Modifier.padding(bottom = 2.dp)
//                    )
//                }
//                IconButton(onClick = onCloseClicked, modifier = modifier.padding(start = 30.dp)) {
//                    Icon(
//                        imageVector = Icons.Default.Close,
//                        contentDescription = stringResource(id = R.string.cd_close_filter)
//                    )
//                }
//            }

            // TITLE TYPE
            /* null value for filterState.titleType means this titles list will not have the option to
            be filtered differently based on title type, and so we shouldn't show the filter chips */
            if (filterState.titleType != null) {
                Divider(modifier = Modifier.padding(vertical = 5.dp))
                FilterLabel(label = stringResource(id = R.string.titlelist_filter_label_type))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
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
        }
        Divider(modifier = Modifier.padding(top = 8.dp))
        ResetAllAndApplyFilterButtonsRow(
            onResetFilterClicked = {
                scoreRange = defaultScoreRange
                yearsRange = defaultYearsRange
                selectedGenres.removeAll(selectedGenres)
                titleTypeFilterSelected = filterState.titleType
            },
            onFilterAppliedClicked = {
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, top = 12.dp),
            resetButtonText = stringResource(id = R.string.titlelist_filter_button_clear_all),
            applyFilterButtonText = stringResource(id = R.string.titlelist_filter_button_apply)
        )
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
    Column(modifier = modifier.fillMaxWidth()){
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
        FilterLabel(label = sectionLabel, onTrailingIconClicked = onCloseFilterClicked)
        LazyColumn(modifier = Modifier.height(400.dp)) {
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
    }
    ResetAllAndApplyFilterButtonsRow(
        onResetFilterClicked = { selectedGenres.removeAll(selectedGenres) },
        onFilterAppliedClicked = { onGenresFilterApplied(selectedGenres) }
    )
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
                        onTitleTypeFilterApplied(TitleType.MOVIE)
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
                        onTitleTypeFilterApplied(TitleType.TV)
                    }
                }
            )
        }
        /* Now showing a Reset All and Apply Filter buttons, because there's no need to add one
        * more click and the same can be achieved just by clicking on wanted title type */
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

    val yearsRangeSelected = remember(key1 = filterState.isYearsRangeDefault()) {
        !filterState.isYearsRangeDefault()
    }
    val scoreRangeSelected = remember(key1 = filterState.isScoreRangeDefault()) {
        !filterState.isScoreRangeDefault()
    }
    val genresSelected = remember(key1 = filterState.genres.isEmpty()) {
        !filterState.genres.isEmpty()
    }

    val titleTypeSelected = remember(key1 = filterState.titleType == null) {
        filterState.titleType != null
    }

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onAllFiltersClicked,
            modifier = modifier.padding(end = 5.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Text(text = stringResource(id = R.string.titlelist_filter_label_all_filters))
        }

        // TITLE TYPE
        if (filterState.titleType != null) {
            SelectableOutlinedButton(
                onClick = { onIndividualFilterButtonClicked(FilterBottomSheetType.TitleType) },
                modifier = modifier
                    .padding(end = 5.dp)
                    .animateContentSize(),
                selected = titleTypeSelected,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(text = getTitleTypeFilterButtonLabel(filterState.titleType))
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = stringResource(id = R.string.cd_expand_more)
                )
            }
        }

        // YEARS RANGE
        SelectableOutlinedButton(
            onClick = {
                onIndividualFilterButtonClicked(FilterBottomSheetType.YearsRange)
            },
            modifier = modifier
                .padding(end = 5.dp)
                .animateContentSize(),
            selected = yearsRangeSelected,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Text(text = getYearsFilterButtonLabel(yearsRange = filterState.getYearsRange(), defaultRange = filterState.getDefaultYearsRange()))
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = stringResource(id = R.string.cd_expand_more)
            )
        }

        // SCORE RANGE
        SelectableOutlinedButton(
            onClick = { onIndividualFilterButtonClicked(FilterBottomSheetType.ScoreRange) },
            modifier = modifier.padding(end = 5.dp),
            selected = scoreRangeSelected,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Text(text = getScoreFilterButtonLabel(
                scoreRange = filterState.getScoreRange(),
                defaultRange = filterState.getDefaultScoreRange()
            ))
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = stringResource(id = R.string.cd_expand_more)
            )
        }

        // GENRES
        SelectableOutlinedButton(
            onClick = { onIndividualFilterButtonClicked(FilterBottomSheetType.Genres) },
            modifier = modifier
                .padding(end = 5.dp)
                .animateContentSize(),
            selected = genresSelected,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Text(text = getGenresFilterButtonLabel(genres = filterState.genres))
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = stringResource(id = R.string.cd_expand_more)
            )
        }
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

@Composable
fun getTitleTypeFilterButtonLabel(titleType: TitleType?): String {
    return when (titleType) {
        TitleType.MOVIE -> stringResource(
            id = R.string.filter_label_title_type_selected, stringResource(
                id = R.string.filter_movies
            )
        )
        TitleType.TV -> stringResource(
            id = R.string.filter_label_title_type_selected, stringResource(
                id = R.string.filter_tv
            )
        )
        null -> ""
    }
}

@Composable
fun getYearsFilterButtonLabel(
    yearsRange: ClosedFloatingPointRange<Float>,
    defaultRange: ClosedFloatingPointRange<Float>
): String {
    return if (yearsRange == defaultRange) {
        stringResource(id = R.string.filter_label_years_unselected)
    } else {
        stringResource(
            id = R.string.filter_label_years_selected,
            yearsRange.start.roundToInt().toString(),
            yearsRange.endInclusive.roundToInt().toString()
        )
    }
}

@Composable
fun getScoreFilterButtonLabel(
    scoreRange: ClosedFloatingPointRange<Float>,
    defaultRange: ClosedFloatingPointRange<Float>
): String {
    return if (scoreRange == defaultRange) {
        stringResource(id = R.string.filter_label_score_unselected)
    } else {
        stringResource(
            id = R.string.filter_label_score_selected,
            scoreRange.start.roundToInt().toString(),
            scoreRange.endInclusive.roundToInt().toString()
        )
    }
}

@Composable
fun getGenresFilterButtonLabel(genres: List<Genre>, ): String {
    return if (genres.isEmpty()) {
        stringResource(id = R.string.filter_label_genres_unselected)
    } else {
        pluralStringResource(
            id = R.plurals.filter_label_genres_selected,
            genres.size,
            if (genres.size > 1) genres.size.toString() else genres[0].name
        )
    }
}



enum class FilterBottomSheetType {
    YearsRange,
    ScoreRange,
    Genres,
    TitleType
}